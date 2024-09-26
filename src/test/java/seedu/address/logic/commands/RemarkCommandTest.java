package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.*;
import static seedu.address.logic.commands.CommandTestUtil.*;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;


public class RemarkCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private static final String REMARK = "remark";


    @Test
    public void equals() {
        final Remark remark = new Remark(VALID_REMARK_AMY);
        final RemarkCommand standardCommand = new RemarkCommand(INDEX_FIRST_PERSON, remark);

        // same values -> returns true
        RemarkCommand commandWithSameValues = new RemarkCommand(INDEX_FIRST_PERSON, remark);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_SECOND_PERSON, remark)));

        // different remark -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("any"))));
    }

    @Test
    public void execute_addRemarkUnfilteredList_success() throws Exception{
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(firstPerson).withRemark(REMARK).build();
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(editedPerson.getRemark().value));
        Model expectedModel = model;
        assertCommandSuccess(remarkCommand, model, remarkCommand.execute(model), expectedModel);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        RemarkCommand remarkCommand = new RemarkCommand(outOfBoundIndex, new Remark(VALID_REMARK_BOB));
        assertCommandFailure(remarkCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());
    }

}
