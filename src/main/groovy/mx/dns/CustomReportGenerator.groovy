package mx.dns

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

public class CustomReportGenerator {

	File cucumberReport

	public CustomReportGenerator(String cucumberReportPath) {
		cucumberReport = new File(cucumberReportPath)
		if (!cucumberReport.exists()) {
			throw new IllegalArgumentException("The cucumber JSON report[$cucumberReportPath] does not exist.")
		}
	}

	public File generate() {
		def report = new CustomReport()

		def scenarioLst = getScenariosAsJson()
		int total = scenarioLst.size()
		int failed = scenarioLst.count { it.result == "failed" }
		int passed = total - failed

		def builder = new JsonBuilder()

		builder.report {
			totalTests total
			testsPassed passed
			testsFailed failed
			scenarios scenarioLst
		}

		println builder.toPrettyString()

		def customReportPath = cucumberReport.getParent() + "/customCucumberReport.json"
		def customReport = new File(customReportPath)
		customReport.text = builder.toPrettyString()

		return customReport
	}

	private List getScenariosAsJson() {
		def cucumberData = new JsonSlurper().parse(cucumberReport)
		def scenarios = []

		cucumberData.each { feature ->
			def featureName = feature.name

			feature.elements.each { scenario ->
				scenario.featureName = featureName
				
				def isFailed = scenario.steps.any { step -> step.result.status != "passed" }
				
				scenario.result = isFailed ? "failed" : "passed"
				scenario.requirement = "NA"
				scenarios.add(scenario)
			}
		}

		return scenarios
	}


	public static void main(String[] args) {
		def gen = new CustomReportGenerator("/home/diego/dev/wa/cucumber-jasper-report/src/main/resources/cucumber.json")
		assert gen.generate().exists() == true
		/*<![CDATA[((net.sf.jasperreports.engine.data.JsonDataSource)$P{REPORT_DATA_SOURCE}).subDataSource("persons")]]>*/
	}

	private static class CustomReport {

		Integer totalTests
		Integer totalTestsPassed
		Integer totalTestsFailed
		List scenarios
	}
}
