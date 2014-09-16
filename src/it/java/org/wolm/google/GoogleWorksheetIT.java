package org.wolm.google;

import static org.fest.assertions.Assertions.*;

import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.wolm.google.GoogleHelper;
import org.wolm.google.GoogleRow;
import org.wolm.google.GoogleSpreadsheet;
import org.wolm.google.GoogleWorksheet;

public class GoogleWorksheetIT {
	GoogleHelper helperUnderTest;

	@Before
	public void beforeEachTest() {
		helperUnderTest = new GoogleHelper("org-wolm-testing");
	}

	@Test
	public void shouldHaveCorrectNumberOfRows() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Persons");
		assertThat(worksheet).isNotNull();
		assert worksheet != null;

		List<GoogleRow> rows = worksheet.getRows();

		// then
		assertThat(rows).isNotNull();
		assertThat(rows).isNotEmpty();
		assertThat(rows.size()).isEqualTo(3);
	}

	@Test
	public void shouldSort() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Persons");
		assertThat(worksheet).isNotNull();
		assert worksheet != null;

		List<GoogleRow> rows = worksheet.getRowsOrderedBy("firstname");
		assertThat(rows).isNotNull();
		assert rows != null;

		// then
		assertThat(rows.get(0).getValue("firstname")).isEqualTo("Kevin");
		assertThat(rows.get(1).getValue("firstname")).isEqualTo("King");
		assertThat(rows.get(2).getValue("firstname")).isEqualTo("Tommy");
	}

	@Test
	public void shouldSortReversed() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Persons");
		assertThat(worksheet).isNotNull();
		assert worksheet != null;

		List<GoogleRow> rows = worksheet.getRowsOrderedBy("firstname", false);
		assertThat(rows).isNotNull();
		assert rows != null;

		// then
		assertThat(rows.get(0).getValue("firstname")).isEqualTo("Tommy");
		assertThat(rows.get(1).getValue("firstname")).isEqualTo("King");
		assertThat(rows.get(2).getValue("firstname")).isEqualTo("Kevin");
	}

	@Test
	public void shouldDetectCellExistance() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Persons");
		assertThat(worksheet).isNotNull();
		assert worksheet != null;

		// then
		assertThat(worksheet.hasCell(-1, "firstname")).isFalse();
		assertThat(worksheet.hasCell(0, "firstname")).isTrue();
		assertThat(worksheet.hasCell(2, "firstname")).isTrue();
		assertThat(worksheet.hasCell(3, "firstname")).isFalse();

		assertThat(worksheet.hasCell(0, "lastname")).isTrue();
		assertThat(worksheet.hasCell(0, "age")).isTrue();
		assertThat(worksheet.hasCell(0, "weight")).isTrue();
		assertThat(worksheet.hasCell(0, "holidaydate")).isTrue();
		assertThat(worksheet.hasCell(0, "species")).isFalse();
		assertThat(worksheet.hasCell(0, "gender")).isFalse();
	}

	@Test
	public void cellsShouldExist() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Persons");
		assertThat(worksheet).isNotNull();
		assert worksheet != null;

		// then
		assertThat(worksheet.getCell(0, "firstname")).isEqualTo("King");
		assertThat(worksheet.getCell(0, "lastname")).isEqualTo("Kong");
		assertThat(worksheet.getCell(0, "age")).isEqualTo("3.72");
		assertThat(worksheet.getCell(0, "weight")).isEqualTo("1200");
		assertThat(worksheet.getCell(0, "holidaydate")).isEqualTo("2014-07-04");

		assertThat(worksheet.getCell(1, "firstname")).isEqualTo("Kevin");
		assertThat(worksheet.getCell(1, "lastname")).isEqualTo("Murray");
		assertThat(worksheet.getCell(1, "age")).isEqualTo("48.75");
		assertThat(worksheet.getCell(1, "weight")).isEqualTo("207");
		assertThat(worksheet.getCell(1, "holidaydate")).isEqualTo("1967-10-18");

		assertThat(worksheet.getCell(2, "firstname")).isEqualTo("Tommy");
		assertThat(worksheet.getCell(2, "lastname")).isEqualTo("Hilfiger");
		assertThat(worksheet.getCell(2, "age")).isEqualTo("-23.4");
		assertThat(worksheet.getCell(2, "weight")).isEqualTo("-34");
		assertThat(worksheet.getCell(2, "holidaydate")).isEqualTo("2004-02-29");
	}

	@Test
	public void cellsShouldNotExist() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Persons");
		assertThat(worksheet).isNotNull();
		assert worksheet != null;

		// then
		assertThat(worksheet.getCell(0, "nunsuch")).isNull();
		assertThat(worksheet.getCell(-2, "lastname")).isNull();
		assertThat(worksheet.getCell(99, "age")).isNull();
	}

	@Test
	public void cellsShouldWorkAsCustomTypes() throws Exception {
		// when
		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assertThat(spreadsheet).isNotNull();
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Persons");
		assertThat(worksheet).isNotNull();
		assert worksheet != null;

		// then
		/* as integers */
		// non-numbers
		assertThat(worksheet.getCellAsLong(0, "firstname")).isNull();
		assertThat(worksheet.getCellAsLong(2, "holidaydate")).isNull();

		// integers
		assertThat(worksheet.getCellAsLong(0, "weight")).isEqualTo(1200);
		assertThat(worksheet.getCellAsLong(1, "weight")).isEqualTo(207);
		assertThat(worksheet.getCellAsLong(2, "weight")).isEqualTo(-34);

		// floats
		assertThat(worksheet.getCellAsLong(0, "age")).isEqualTo(3);
		assertThat(worksheet.getCellAsLong(1, "age")).isEqualTo(48);
		assertThat(worksheet.getCellAsLong(2, "age")).isEqualTo(-23);

		/* as doubles */
		// non-numbers
		assertThat(worksheet.getCellAsDouble(0, "firstname")).isNull();
		assertThat(worksheet.getCellAsDouble(2, "holidaydate")).isNull();

		// integers
		assertThat(worksheet.getCellAsDouble(0, "weight")).isEqualTo(1200.0);
		assertThat(worksheet.getCellAsDouble(1, "weight")).isEqualTo(207.0);
		assertThat(worksheet.getCellAsDouble(2, "weight")).isEqualTo(-34.0);

		// floats
		assertThat(worksheet.getCellAsDouble(0, "age")).isEqualTo(3.72);
		assertThat(worksheet.getCellAsDouble(1, "age")).isEqualTo(48.75);
		assertThat(worksheet.getCellAsDouble(2, "age")).isEqualTo(-23.4);

		/* as dates */
		// non-dates
		assertThat(worksheet.getCellAsDate(0, "firstname")).isNull();
		assertThat(worksheet.getCellAsDate(2, "weight")).isNull();

		// dates
		assertThat(worksheet.getCellAsDate(0, "holidaydate")).isEqualTo(new GregorianCalendar(2014, 6, 4).getTime());
		assertThat(worksheet.getCellAsDate(1, "holidaydate")).isEqualTo(new GregorianCalendar(1967, 9, 18).getTime());
		assertThat(worksheet.getCellAsDate(2, "holidaydate")).isEqualTo(new GregorianCalendar(2004, 1, 29).getTime());

	}

}
