package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.Date;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentList;
import seedu.address.model.assignment.exceptions.AssignmentNotFoundException;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.TutorialClass;
import seedu.address.model.tut.Tutorial;
import seedu.address.model.tut.TutorialList;
import seedu.address.model.tut.exceptions.TutNoFoundException;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Student> filteredStudents;
    private final AssignmentList assignmentList;

    private final TutorialList tutorials;
    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs,
                        AssignmentList assignmentList, TutorialList tutorialList) {
        //TODO: Add sample tutorialList for the test cases (see getTypicalStudentsList())
        requireAllNonNull(addressBook, userPrefs, assignmentList, tutorialList);

        logger.fine("Initializing with address book: " + addressBook + ", user prefs " + userPrefs
            + "and assignment list: " + assignmentList);
        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredStudents = new FilteredList<>(this.addressBook.getStudentList());
        this.assignmentList = assignmentList;
        tutorials = tutorialList;
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs(), new AssignmentList(), new TutorialList());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasStudent(Student student) {
        requireNonNull(student);
        return addressBook.hasStudent(student);
    }

    @Override
    public void deleteStudent(Student target) {
        addressBook.removeStudent(target);
    }

    @Override
    public void addStudent(Student student) {
        addressBook.addStudent(student);
        updateFilteredStudentList(PREDICATE_SHOW_ALL_STUDENTS);
    }

    @Override
    public void setStudent(Student target, Student editedStudent) {
        requireAllNonNull(target, editedStudent);
        addressBook.setStudent(target, editedStudent);
    }

    //=========== Tutorial ================================================================================

    @Override
    public void addTutorial(Tutorial tutorial) {
        requireNonNull(tutorial);
        tutorials.addTutorial(tutorial);
    }

    @Override
    public TutorialList getTutorialList() {
        return tutorials;
    }

    @Override
    public boolean hasTutorial(Tutorial tutorial) {
        requireNonNull(tutorial);
        return tutorials.hasTutorial(tutorial);
    }

    @Override
    public boolean hasTutorial(TutorialClass tutorialClass) {
        requireNonNull(tutorialClass);
        return tutorials.hasTutorial(tutorialClass);
    }

    @Override
    public boolean setStudentAttendance(StudentId target, TutorialClass tut, Date date) {
        boolean isSuccess = tutorials.getTutorials().stream()
                .filter(s -> s.getTutorialClass().equals(tut))
                .findFirst()
                .map(tutorial -> tutorial.setAttendance(date, target))
                .orElse(false);
        updateFilteredStudentList(PREDICATE_SHOW_ALL_STUDENTS);
        return isSuccess;
    }

    @Override
    public void deleteTutorial(Tutorial tutorial) {
        requireNonNull(tutorial);
        tutorials.deleteTutorial(tutorial);
    }

    @Override
    public void assignStudent(Student student, TutorialClass tutorialClass) {
        requireNonNull(student);
        requireNonNull(tutorialClass);
        if (!tutorials.hasTutorial(tutorialClass)) {
            throw new TutNoFoundException();
        }
        tutorials.assignStudent(student, tutorialClass);
    }

    //=========== Assignment ================================================================================

    @Override
    public AssignmentList getAssignmentList() {
        return assignmentList;
    }

    @Override
    public boolean hasAssignment(Assignment assignment) {
        requireNonNull(assignment);

        return assignmentList.hasAssignment(assignment);
    }

    @Override
    public void addAssignment(Assignment assignment) {
        requireNonNull(assignment);

        assignmentList.addAssignment(assignment);
    }

    @Override
    public void deleteAssignment(Assignment assignment) {
        assignmentList.deleteAssignment(assignment);
    }

    @Override
    public String checkAssignment(Assignment assignment) throws AssignmentNotFoundException {
        requireNonNull(assignment);

        return assignmentList.getStatus(assignment, addressBook.getStudentList());
    }

    @Override
    public void setAssignmentStatus(Assignment assignment, Student targetStudent, boolean newStatus)
            throws AssignmentNotFoundException {
        requireAllNonNull(targetStudent, newStatus);

        assignmentList.setStatus(assignment, targetStudent, newStatus);
    }

    @Override
    public String listAssignments() {
        return assignmentList.toString();
    }

    //=========== Filtered Student List (student ID)

    @Override
    public boolean hasStudentWithId(StudentId studentId) {
        requireNonNull(studentId);
        return addressBook.hasStudentId(studentId);
    }

    //=========== Filtered Student List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Student} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Student> getFilteredStudentList() {
        return filteredStudents;
    }

    @Override
    public void updateFilteredStudentList(Predicate<Student> predicate) {
        requireNonNull(predicate);
        filteredStudents.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredStudents.equals(otherModelManager.filteredStudents)
                && assignmentList.equals(otherModelManager.assignmentList);
    }

}
