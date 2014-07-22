package org.wolm.google;

import static org.fest.assertions.Assertions.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.wolm.google.GoogleHelper;
import org.wolm.google.GoogleRow;
import org.wolm.google.GoogleSpreadsheet;
import org.wolm.google.GoogleWorksheet;
import org.wolm.google.RowFilter;
import org.wolm.google.RowFilter_MaxCount;
import org.wolm.google.RowFilter_Value;
import org.wolm.google.RowFilter_ValueStartsWith;
import org.wolm.google.RowFilter_Weekday;

public class RowFilterIT {
	static GoogleHelper helperUnderTest;
	static List<GoogleRow> rowsUnderTest;

	@BeforeClass
	public static void beforeEachTest() throws Exception {
		helperUnderTest = new GoogleHelper("org-wolm-testing");

		GoogleSpreadsheet spreadsheet = helperUnderTest.getSpreadsheet("zGData Test Sheet");
		assert spreadsheet != null;

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Persons");
		assert worksheet != null;

		rowsUnderTest = worksheet.getRows();
		assert rowsUnderTest != null;
	}

	@Test
	public void maxCount() throws Exception {
		// then
		assertThat(rowsUnderTest.size()).isEqualTo(3);

		List<GoogleRow> rows = RowFilter.filter(rowsUnderTest, new RowFilter_MaxCount(2));
		assertThat(rowsUnderTest.size()).isEqualTo(3); // didn't change original list

		// first two, in order
		assertThat(rows.size()).isEqualTo(2);
		assertThat(rows.get(0).getValue("firstname")).isEqualTo("King");
		assertThat(rows.get(1).getValue("firstname")).isEqualTo("Kevin");
	}

	@Test
	public void weekday() throws Exception {
		// then
		assertThat(rowsUnderTest.size()).isEqualTo(3);

		List<GoogleRow> rows = RowFilter.filter(rowsUnderTest, new RowFilter_Weekday("holidaydate", "Wednesday"));
		assertThat(rowsUnderTest.size()).isEqualTo(3); // didn't change original list

		assertThat(rows.size()).isEqualTo(1);
		assertThat(rows.get(0).getValue("firstname")).isEqualTo("Kevin");
	}

	@Test
	public void weekdays() throws Exception {
		// then
		assertThat(rowsUnderTest.size()).isEqualTo(3);

		List<GoogleRow> rows = RowFilter.filter(rowsUnderTest, new RowFilter_Weekday("holidaydate", "Wednesday",
				"Friday"));
		assertThat(rowsUnderTest.size()).isEqualTo(3); // didn't change original list

		assertThat(rows.size()).isEqualTo(2);
		assertThat(rows.get(0).getValue("firstname")).isEqualTo("King");
		assertThat(rows.get(1).getValue("firstname")).isEqualTo("Kevin");
	}

	@Test
	public void value() throws Exception {
		// then
		assertThat(rowsUnderTest.size()).isEqualTo(3);

		List<GoogleRow> rows = RowFilter.filter(rowsUnderTest, new RowFilter_Value("lastname", "Kong"));
		assertThat(rowsUnderTest.size()).isEqualTo(3); // didn't change original list

		assertThat(rows.size()).isEqualTo(1);
		assertThat(rows.get(0).getValue("firstname")).isEqualTo("King");
	}

	@Test
	public void valueStartsWith() throws Exception {
		// then
		assertThat(rowsUnderTest.size()).isEqualTo(3);

		List<GoogleRow> rows = RowFilter.filter(rowsUnderTest, new RowFilter_ValueStartsWith("age", "-"));
		assertThat(rowsUnderTest.size()).isEqualTo(3); // didn't change original list

		assertThat(rows.size()).isEqualTo(1);
		assertThat(rows.get(0).getValue("firstname")).isEqualTo("Tommy");
	}
}
