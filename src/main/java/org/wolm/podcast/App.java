package org.wolm.podcast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.wolm.aws.AwsS3Helper;
import org.wolm.google.GoogleHelper;
import org.wolm.google.GoogleRow;
import org.wolm.google.GoogleSpreadsheet;
import org.wolm.google.GoogleWorksheet;
import org.wolm.google.RowFilter;
import org.wolm.google.RowFilter_MaxCount;
import org.wolm.google.RowFilter_Value;
import org.wolm.google.RowFilter_ValueStartsWith;

import com.amazonaws.services.s3.model.Bucket;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * Produce a podcast XML file for WOLM Sunday Services
 */
@Parameters(separators = "=")
public class App {
	private static final String PODCAST_BUCKET_NAME = "wordoflife.mn.podcast";
	private static final String PODCAST_KEY = "wolmn-service-podcast.rss.xml";

	private final SimpleDateFormat rssFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	private final SimpleDateFormat yearMonthDayFormatter = new SimpleDateFormat("yyyy-MM-dd");

	private final GoogleHelper googleHelper;
	private final AwsS3Helper s3Helper;

	/* Command Parameters */
	@Parameter(names = "--help", description = "This help page.", help = true)
	private boolean helpRequested = false;

	@Parameter(names = "--verbose", description = "Provide status output.")
	private boolean verbose = false;

	@Parameter(names = "--messageLog", description = "Name of the spreadsheet on the Google Drive that contains the message log")
	private String spreadsheetName = "Messages";

	@Parameter(names = "--worksheet", description = "Name of the worksheet in the spreasheet that contains the message log")
	private String worksheetName = "Media Log";

	@Parameter(names = "--length", description = "Number of messages to include in the podcast. "
			+ "This (with --day) determines which messages are selected for inclusion in the podcast.")
	private int maximumMessagesInPodcast = 6;

	@Parameter(names = "--day", description = "Day of the week to build the podcast for. Typically Sunday or Wednesday. "
			+ "This (with --length) determines which messages are selected for inclusion in the podcast.")
	private String weekdayName = "Sunday";

	@Parameter(names = "--out", description = "Path of the file to write the podcast to. "
			+ "If this is not supplied, then the podcast will be written to the console.")
	private String outFilePath;

	@Parameter(names = "--upload", description = "Specify this if you want the podcast automatically uploaded to Amazon S3 when completed. "
			+ "If you specify an --out parameter, then the file will be written to that file, then uploaded. "
			+ "If you do not specify an --out parameter, but do specify --upload, then the podcast will be written to a "
			+ "temporary file instead of the console.")
	private boolean podcastUploadedWhenDone = false;

	/**
	 * Main CLI interface
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// create app
		App app = new App(new GoogleHelper("org-wolm-podcast"), new AwsS3Helper());
		JCommander jCommander = new JCommander(app, args);
		if (app.isHelpRequested()) {
			jCommander.usage();
			System.exit(0);
		}

		app.podcast();
	}

	public App(GoogleHelper googleHelper, AwsS3Helper s3Helper) {
		super();
		this.googleHelper = googleHelper;
		this.s3Helper = s3Helper;
	}

	public boolean isHelpRequested() {
		return helpRequested;
	}

	public void setHelpRequested(boolean help) {
		this.helpRequested = help;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public String getSpreadsheetName() {
		return spreadsheetName;
	}

	public void setSpreadsheetName(String spreadsheetName) {
		this.spreadsheetName = spreadsheetName;
	}

	public String getWorksheetName() {
		return worksheetName;
	}

	public void setWorksheetName(String worksheetName) {
		this.worksheetName = worksheetName;
	}

	public int getMaximumMessagesInPodcast() {
		return maximumMessagesInPodcast;
	}

	public void setMaximumMessagesInPodcast(int maximumMessagesInPodcast) {
		this.maximumMessagesInPodcast = maximumMessagesInPodcast;
	}

	public String getWeekdayName() {
		return weekdayName;
	}

	public void setWeekdayName(String weekdayName) {
		this.weekdayName = weekdayName;
	}

	public String getOutFilePath() {
		return outFilePath;
	}

	public void setOutFilePath(String outFilePath) {
		this.outFilePath = outFilePath;
	}

	public boolean isPodcastUploadedWhenDone() {
		return podcastUploadedWhenDone;
	}

	public void setPodcastUploadedWhenDone(boolean uploadPodcast) {
		this.podcastUploadedWhenDone = uploadPodcast;
	}

	public GoogleHelper getHelper() {
		return googleHelper;
	}

	public void podcast() throws FileNotFoundException, Exception {
		// determine output vectors
		PrintStream outStream = System.out;
		File outFile = null;
		// we always have to write to a file when uploading
		if (getOutFilePath() != null || isPodcastUploadedWhenDone()) {
			// get the name of the file to write to. use a tmp one if not provided
			if (getOutFilePath() == null) setOutFilePath(System.getProperty("java.io.tmpdir") + "/" + PODCAST_KEY);

			// validate the output path and delete existing file
			outFile = new File(getOutFilePath());
			File outPath = outFile.getParentFile();
			if (!outPath.exists()) throw new FileNotFoundException(outPath.getAbsolutePath());
			if (outFile.exists()) outFile.delete();

			// create a stream for the file
			outStream = new PrintStream(outFile);
		}

		// if outputting to a file, wire the status to stdout
		// otherwise, wire the status to stderr only if verbose mode
		PrintStream statusStream = null;
		if (outStream != System.out) statusStream = System.out;
		else if (isVerbose()) statusStream = System.err;

		// write the podcast
		generatePodcast(outStream, statusStream);
		if (outStream != System.out) outStream.close();

		// upload the podcast
		if (outFile != null && isPodcastUploadedWhenDone()) {
			uploadPodcast(outFile, statusStream);
		}

		if (statusStream != null && statusStream != System.out && statusStream != System.err) statusStream.close();
	}

	/**
	 * Reads the WOLM message log and outputs a podcast that matches the command line parameters.
	 * 
	 * @param cmd Command line parameters
	 * @param outStream Stream to write the output podcast to
	 * @return <code>true</code> on success, <code>false</code> on failure
	 * @throws ServiceException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public boolean generatePodcast(PrintStream outStream, PrintStream statusStream) throws Exception {
		// get the spreadsheet
		if (statusStream != null) {
			statusStream.println("Retrieving '" + getSpreadsheetName() + "' spreadsheet from Google...");
		}
		GoogleSpreadsheet spreadsheet = getHelper().getSpreadsheet(getSpreadsheetName());
		if (spreadsheet == null) {
			throw new Exception("ERROR: Cannot find spreadsheet titled '" + getSpreadsheetName() + "'");
		}

		// get worksheet
		GoogleWorksheet worksheet = spreadsheet.getWorksheet(getWorksheetName());
		if (worksheet == null) {
			throw new Exception("ERROR: Cannot find worksheet titled '" + getWorksheetName() + "' in the spreadsheet '"
					+ getWorksheetName() + "'");
		}
		if (!worksheet.hasColumn("visibility")) {
			throw new Exception("Worksheet '" + getWorksheetName() + "' has no 'visibility' data.");
		}
		if (!worksheet.hasColumn("audiolink")) {
			throw new Exception("Worksheet '" + getWorksheetName() + "' has no 'audiolink' data.");
		}

		if (!worksheet.hasColumn("playlist")) {
			throw new Exception("Worksheet '" + getWorksheetName() + "' has no 'playlist' data.");
		}

		// get all rows
		List<GoogleRow> rows = worksheet.getRowsOrderedBy("date", false);
		if (rows == null) {
			System.err.println("ERROR: Worksheet '" + getWorksheetName() + "' contains no data to read");
			return false;
		}

		// start filtering the list of rows until we have something appropriate for the podcast
		rows = RowFilter.filter(rows, new RowFilter_Value("visibility", "Public"));
		rows = RowFilter.filter(rows, new RowFilter_ValueStartsWith("audiolink", "http"));
		rows = RowFilter.filter(rows, new RowFilter_Value("playlist", "Service"));
		rows = RowFilter.filter(rows, new RowFilter_MaxCount(getMaximumMessagesInPodcast()));

		// reverse so they are in chronological order again
		Collections.reverse(rows);

		// printWorksheetRows(worksheet, rows);

		outputPodcastHeader(outStream, statusStream);
		if (statusStream != null) statusStream.println("Exporting " + rows.size() + " recent services...");
		int index = 1;
		for (GoogleRow row : rows) {
			if (statusStream != null) statusStream.print("  " + (index++) + ". ");
			outputMessageItem(row, outStream, statusStream);
		}
		outputPodcastFooter(outStream, statusStream);

		return true;
	}

	private void outputPodcastHeader(PrintStream outStream, PrintStream statusStream) {
		outStream.println("<?xml version=\"1.0\"?>");
		outStream.println("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
		outStream.println("  <channel>");
		outStream.println("    <title>Word of Life Ministries: Sunday</title>");
		outStream.println("    <link>http://www.wordoflifemn.org/</link>");
		outStream.println("    <description>Podcast of Word of Life Ministries Sunday services</description>");
		outStream.println("    <language>en-us</language>");
		outStream.println("    <copyright>Copyright 2014 Word of Life Ministries</copyright>");
		outStream.println("    <managingEditor>wordoflife.mn@gmail.com (Word of Life Ministries)</managingEditor>");
		outStream.println("    <webMaster>kevmurray@me.com (Kevin Murray)</webMaster>");
		outStream.println("    <category>Christian Sermon</category>");
		outStream.println("    <ttl>60</ttl>");
		outStream.println("    <image>");
		outStream
				.println("      <url>https://s3-us-west-2.amazonaws.com/wordoflife.mn.podcast/WordofLifeMinD72aR05aP01ZL.png</url>");
		outStream.println("      <title>Word of Life Ministries</title>");
		outStream.println("      <link>http://www.wordoflifemn.org/</link>");
		outStream.println("    </image>");
		outStream
				.println("    <atom:link href=\"http://s3-us-west-2.amazonaws.com/wordoflife.mn.podcast/wolmn-service-podcast.rss.xml\" "
						+ "rel=\"self\" type=\"application/rss+xml\" />");
	}

	private void outputMessageItem(GoogleRow item, PrintStream outStream, PrintStream statusStream) throws Exception {
		// date (use 10:00 AM)
		Date date = item.getDateValue("date");
		if (date == null) throw new Exception("Cannot process items without dates");
		date = new Date(date.getTime() + (10 * DateUtils.MILLIS_PER_HOUR)); // cheesy - should use calendar
		String dateAsString = rssFormatter.format(date);

		// title
		String title = item.getValue("name");
		if (StringUtils.isBlank(title)) throw new Exception("Empty title from row " + date);

		if (statusStream != null) {
			statusStream.println(title + " (" + yearMonthDayFormatter.format(date) + ")");
		}

		// description
		String description = item.getValue("description");
		if (StringUtils.isBlank(description)) {
			if (statusStream != null) statusStream.println("      WARNING: No description for " + title);
			description = title + " message from " + yearMonthDayFormatter.format(date);
		}

		// audio URL
		String audioUrl = item.getValue("audiolink");
		if (StringUtils.isBlank(audioUrl)) throw new Exception(title + " has no audio link");

		Long sizeInBytes = getUrlContentLength(audioUrl);

		outStream.println("    <item>");
		outStream.println("      <title>" + StringEscapeUtils.escapeXml10(title) + "</title>");
		outStream.println("      <description>" + StringEscapeUtils.escapeXml10(description) + "</description>");
		outStream.println("      <author>wordoflife.mn@gmail.com (Word of Life Ministries)</author>");
		outStream.println("      <category>Christian Sermon</category>");
		outStream.println("      <guid>" + audioUrl + "</guid>");
		outStream.println("      <pubDate>" + dateAsString + "</pubDate>");
		outStream.println("      <enclosure url=\"" + audioUrl + "\" length=\""
				+ (sizeInBytes == null ? 50000000 : sizeInBytes) + "\" type=\"audio/mpeg\" />");
		outStream.println("    </item>");
	}

	private void outputPodcastFooter(PrintStream outStream, PrintStream statusStream) {
		outStream.println("  </channel>");
		outStream.println("</rss>");
	}

	private Long getUrlContentLength(String urlAddress) {
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(urlAddress);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("HEAD");
			urlConnection.getInputStream();
			return (long) urlConnection.getContentLength();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (urlConnection != null) urlConnection.disconnect();
		}
		return null;
	}

	public void uploadPodcast(File file, PrintStream statusStream) throws Exception {
		if (statusStream != null) statusStream.println("Uploading podcast to Amazon...");

		Bucket podcastBucket = s3Helper.getBucket(PODCAST_BUCKET_NAME);
		if (podcastBucket == null) throw new Exception("Cannot find the podcast bucket: '" + PODCAST_BUCKET_NAME + "'");
		s3Helper.uploadPublicFile(podcastBucket, PODCAST_KEY, file);

		if (statusStream != null) statusStream.println("  Uploaded " + PODCAST_BUCKET_NAME + ":" + PODCAST_KEY);
	}

	public void printWorksheetStats(GoogleWorksheet worksheet) {
		System.out.println("Worksheet: " + worksheet.getTitle());
		System.out.println("  Columns: " + worksheet.getColumnCount());
		System.out.println("     Rows: " + worksheet.getRowCount());
		System.out.println("  CanEdit: " + worksheet.getCanEdit());
	}

	public void printWorksheetRows(GoogleWorksheet worksheet) throws AuthenticationException, IOException,
			ServiceException {
		printWorksheetRows(worksheet, worksheet.getRows());
	}

	public void printWorksheetRows(GoogleWorksheet worksheet, List<GoogleRow> rows) throws AuthenticationException,
			IOException, ServiceException {
		System.out.println("Worksheet: " + worksheet.getTitle());

		// build list of columns (from set)
		List<String> columnNames = worksheet.getColumnNames();

		// print header row
		for (String columnName : columnNames)
			System.out.print(columnName + "\t");
		System.out.println();

		// print data rows
		for (GoogleRow row : rows) {
			for (String columnName : columnNames)
				System.out.print(row.getValue(columnName) + "\t");
			System.out.println();
		}
	}

}
