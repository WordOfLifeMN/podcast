package org.wolm.google;

import static org.fest.assertions.Assertions.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.wolm.google.GoogleHelper;
import org.wolm.google.GoogleSpreadsheet;
import org.wolm.google.GoogleWorksheet;

public class GoogleHelperIT {
	GoogleHelper helperUnderTest;

	@Before
	public void beforeEachTest() {
		helperUnderTest = new GoogleHelper("org-wolm-testing");
	}

	@Test
	public void shouldBeMultipleSpreadsheets() throws Exception {
		// when
		List<GoogleSpreadsheet> spreadsheets = helperUnderTest.getAllSpreadsheets();
		// then
		assertThat(spreadsheets).isNotEmpty();
	}

	@Test
	public void shouldHaveMessagesSpreadsheet() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		// then
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;
		assertThat(spreadsheet.getTitle()).isEqualTo("zGData Test Sheet");
	}

	@Test
	public void shouldNotHaveNunsuchSpreadsheet() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("Nunsuch");
		// then
		assertThat(spreadsheet).isNull();
	}

	@Test
	public void shouldHaveMessagesSpreadsheetWorksheets() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;
		GoogleWorksheet persons = spreadsheet.getWorksheet("Persons");
		GoogleWorksheet jobs = spreadsheet.getWorksheet("Jobs");
		// then
		assertThat(persons).isNotNull();
		assert persons != null;
		assertThat(persons.getTitle()).isEqualTo("Persons");

		assertThat(jobs).isNotNull();
		assert jobs != null;
		assertThat(jobs.getTitle()).isEqualTo("Jobs");
	}

	@Test
	public void shouldNotHaveMessagesSpreadsheetNunsuchWorksheet() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Nunsuch");

		// then
		assertThat(worksheet).isNull();
	}

}
