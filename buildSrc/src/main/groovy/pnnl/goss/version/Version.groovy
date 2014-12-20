package pnnl.goss.version

import java.util.Date
import java.text.SimpleDateFormat

class Version {
	String originalVersion
	String thisVersion
	String status
	Date buildTime

	Version(String versionValue) {
		buildTime = new Date()
		originalVersion = versionValue
		if (originalVersion.endsWith('-SNAPSHOT')) {
			status = 'integration'
			thisVersion = originalVersion // originalVersion.substring(0, originalVersion.length() - 'SNAPSHOT'.length()) + getTimestamp()
		} else {
			status = 'release'
			thisVersion = versionValue
		}
	}

	String getTimestamp() {
		// Convert local file timestamp to UTC
		def format = new SimpleDateFormat('yyyyMMddHHmmss')
		format.setCalendar(Calendar.getInstance(TimeZone.getTimeZone('UTC')));
		return format.format(buildTime)
	}

	String toString() {
		thisVersion
	}
}