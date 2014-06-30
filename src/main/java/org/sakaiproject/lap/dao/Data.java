/**
 * Copyright 2008 Sakaiproject Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.lap.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.lap.Constants;
import org.sakaiproject.lap.service.CsvService;
import org.sakaiproject.lap.service.DateService;
import org.sakaiproject.lap.service.FileService;

/**
 * General data-gathering methods, from database and other operations
 * Sends data out to REST endpoints and views
 * 
 * @author Robert E. Long (rlong @ unicon.net)
 *
 */
public class Data extends Database {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * Convenience method for the activity method
     * @param directory the name of the date-specific directory to store the .csv file
     * @return true, if creation and storage successful
     */
    public boolean prepareActivityCsv(String directory) {
        return prepareActivityCsv("", "", "", directory);
    }

    /**
     * Create the activity.csv file, store it on file system
     * 
     * @param criteria the site title search term to use
     * @param startDate the date to start the inclusion of data
     * @param endDate the date to end the inclusion of data
     * @param directory the name of the date-specific directory to store the .csv file
     * @return true, if creation and storage successful
     */
    public boolean prepareActivityCsv(String criteria, String startDate, String endDate, String directory) {
        boolean hasStartDate = StringUtils.isNotBlank(startDate);
        boolean hasEndDate = StringUtils.isNotBlank(endDate);
        boolean success = false;
        PreparedStatement preparedStatement = null;

        try {
            String query = sql.getSqlEvents(hasStartDate, hasEndDate);

            preparedStatement = createPreparedStatement(preparedStatement, query);
            preparedStatement.setString(1, "%" + criteria + "%");

            if (hasStartDate) {
                startDate += DateService.DATE_START_TIME;
                preparedStatement.setString(2, startDate);
            }

            if (hasEndDate) {
                endDate += DateService.DATE_END_TIME;
                if (hasStartDate) {
                    preparedStatement.setString(3, endDate);
                } else {
                    preparedStatement.setString(2, endDate);
                }
            }

            success = saveResultsToFile(preparedStatement, directory, Constants.CSV_FILE_ACTIVITY);
        } catch (Exception e) {
            log.error("Error preparing activity csv: " + e, e);
        } finally {
            closePreparedStatement(preparedStatement);
        }

        return success;
    }

    /**
     * Convenience method for the courses method
     * @param directory the name of the date-specific directory to store the .csv file
     * @return true, if creation and storage successful
     */
    public boolean prepareCoursesCsv(String directory) {
        return prepareCoursesCsv("", directory);
    }

    /**
     * Create the courses.csv file, store it on file system
     * 
     * @param criteria the site title search term to use
     * @param directory the name of the date-specific directory to store the .csv file
     * @return true, if creation and storage successful
     */
    public boolean prepareCoursesCsv(String criteria, String directory) {
        boolean success = false;
        PreparedStatement preparedStatement = null;

        try {
            String query = sql.getSqlCourses();

            /*preparedStatement = createPreparedStatement(preparedStatement, query);
            preparedStatement.setString(1, "%" + criteria + "%");

            success = saveToFile(preparedStatement, directory, Constants.CSV_FILE_COURSES);*/
        } catch (Exception e) {
            log.error("Error preparing courses csv: " + e, e);
        } finally {
            closePreparedStatement(preparedStatement);
        }

        return success;
    }

    /**
     * Convenience method for the grades method
     * @param directory the name of the date-specific directory to store the .csv file
     * @return true, if creation and storage successful
     */
    public boolean prepareGradesCsv(String directory) {
        return prepareGradesCsv("", directory);
    }

    /**
     * Create the grades.csv file, store it on file system
     * 
     * @param criteria the site title search term to use
     * @param directory the name of the date-specific directory to store the .csv file
     * @return true, if creation and storage successful
     */
    public boolean prepareGradesCsv(String criteria, String directory) {
        boolean success = false;
        PreparedStatement preparedStatement = null;

        try {
            String query = sql.getSqlGrades();

            preparedStatement = createPreparedStatement(preparedStatement, query);
            preparedStatement.setString(1, "%" + criteria + "%");

            success = saveResultsToFile(preparedStatement, directory, Constants.CSV_FILE_GRADES);
        } catch (Exception e) {
            log.error("Error preparing grades csv: " + e, e);
        } finally {
            closePreparedStatement(preparedStatement);
        }

        return success;
    }

    /**
     * Convenience method for the students method
     * @param directory the name of the date-specific directory to store the .csv file
     * @return true, if creation and storage successful
     */
    public boolean prepareStudentsCsv(String directory) {
        return prepareStudentsCsv("", directory);
    }

    /**
     * Create the students.csv file, store it on file system
     * 
     * @param criteria the site title search term to use
     * @param directory the name of the date-specific directory to store the .csv file
     * @return true, if creation and storage successful
     */
    public boolean prepareStudentsCsv(String criteria, String directory) {
        boolean success = false;
        PreparedStatement preparedStatement = null;

        try {
            String query = sql.getSqlStudents();

            /*preparedStatement = createPreparedStatement(preparedStatement, query);
            preparedStatement.setString(1, "%" + criteria + "%");

            success = saveToFile(preparedStatement, directory, Constants.CSV_FILE_STUDENTS);*/
        } catch (Exception e) {
            log.error("Error preparing students csv: " + e, e);
        } finally {
            closePreparedStatement(preparedStatement);
        }

        return success;
    }

    /**
     * Processes the results from a query
     * 1. Executes the prepared statement
     * 2. Creates CSV strings
     * 3. Saves CSV string to a file
     * 
     * @param preparedStatement the prepared statement
     * @param directory the directory to save the file in
     * @param fileName the name of the file
     * @return true, if successful operations
     * @throws Exception on errors
     */
    private boolean saveResultsToFile(PreparedStatement preparedStatement, String directory, String fileName) throws Exception {
        ResultSet results = executePreparedStatement(preparedStatement);
        ResultSetMetaData metadata = results.getMetaData();
        int numberOfColumns = metadata.getColumnCount();

        // header row
        List<String> header = new ArrayList<String>();
        for (int i = 1;i <=numberOfColumns;i++) {
            header.add(metadata.getColumnLabel(i));
        }
        String csvData = csvService.setAsCsvRow(header);

        // data rows
        while (results.next()) {
            List<String> row = new ArrayList<String>();
            for (int i = 1;i <=numberOfColumns;i++) {
                row.add(results.getString(i));
            }
            csvData += csvService.setAsCsvRow(row);
        }

        boolean success = fileService.saveStringToFile(csvData, directory, fileName);

        return success;
    }

    /**
     * Retrieves a CSV file's contents as a string
     * 
     * @param directory the directory containing the file
     * @param fileName the name of the file
     * @return the CSV data as a string
     */
    public String getCsvDataString(String directory, String fileName) {
        String csvData = fileService.readFileIntoString(directory, fileName);

        return csvData;
    }

    /**
     * Retrieves the listing of subdirectories in the configured storage path
     * 
     * @return HashMap of the listings (e.g. "yyyyMMdd_HHmmss" => "MMM dd, yyyy HH:mm:ss")
     */
    public Map<String, String> getDirectoryListing() {
        String directory = fileService.getStoragePath();

        return getDirectoryListing(directory);
    }

    /**
     * Retrieves the listing of subdirectories in the given directory
     * 
     * @return HashMap of the listings (e.g. "yyyyMMdd_HHmmss" => "MMM dd, yyyy HH:mm:ss")
     */
    public Map<String, String> getDirectoryListing(String directory) {
        if (StringUtils.isBlank(directory)) {
            directory = fileService.getStoragePath();
        }

        List<String> directoryNames = fileService.parseDirectory(directory);
        Map<String, String> directories = new LinkedHashMap<String, String>(directoryNames.size());

        for (String directoryName : directoryNames) {
            try {
                Date date = DateService.SDF_FILE_NAME.parse(directoryName);
                String displayDate = DateService.SDF_DISPLAY.format(date);
                directories.put(directoryName, displayDate);
            } catch (Exception e) {
                log.error("Error parsing directory name: " + e, e);
                directories.put(directoryName, directoryName);
            }
        }

        return directories;
    }

    /**
     * Gets the last data generation date from the listing of directories
     * 
     * @param directoryListing the directory listing map
     * @return the last generation date
     */
    public String getLatestRunDate() {
        Map<String, String> directoryListing = getDirectoryListing();

        return dateService.getLatestRunDate(directoryListing);
    }

    /**
     * Gets the next scheduled automatic generation date
     * 
     * @return the next generation date
     */
    public String getNextScheduledRunDate() {
        return dateService.getNextScheduledRunDate();
    }

    private CsvService csvService;
    public void setCsvService(CsvService csvService) {
        this.csvService = csvService;
    }

    private DateService dateService;
    public void setDateService(DateService dateService) {
        this.dateService = dateService;
    }

    private FileService fileService;
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    private Sql sql;
    public void setSql(Sql sql) {
        this.sql = sql;
    }

}
