// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

import com.daimler.sechub.domain.scan.report.ScanReportResult;
import com.daimler.sechub.domain.scan.report.ScanReportTrafficLightCalculator;
import com.daimler.sechub.sharedkernel.type.TrafficLight;

public class HTMLScanResultReportModelBuilderTest {

	/* we use own variables here and not the constants from class to test, to separate test from impl...*/
	private static final String SHOW_LIGHT = "opacity: 1.0";
	private static final String HIDE_LIGHT ="opacity: 0.25";
	
	private HTMLScanResultReportModelBuilder builderToTest;
	private ScanReportResult scanResult;
	private ScanReportTrafficLightCalculator trafficLightCalculator;
	private SecHubResult result;
	private List<SecHubFinding> greenList;
	private List<SecHubFinding> redList;
	private List<SecHubFinding> yellowList;

	@Before
	public void before() throws Exception {
		builderToTest = new HTMLScanResultReportModelBuilder();
		
		trafficLightCalculator = mock(ScanReportTrafficLightCalculator.class);
		Resource cssResource = mock(Resource.class);
		when (cssResource.getInputStream()).thenReturn(new ByteArrayInputStream("csscontentfromstream".getBytes()));

		builderToTest.cssResource=cssResource;
		builderToTest.trafficLightCalculator=trafficLightCalculator;
		
		result = mock(SecHubResult.class);
		
		scanResult= mock(ScanReportResult.class);
		when(scanResult.getResult()).thenReturn(result);

		greenList=new ArrayList<>();
		redList=new ArrayList<>();
		yellowList=new ArrayList<>();
		
		when(trafficLightCalculator.filterFindingsFor(result, TrafficLight.RED)).thenReturn(redList);
		when(trafficLightCalculator.filterFindingsFor(result, TrafficLight.YELLOW)).thenReturn(yellowList);
		when(trafficLightCalculator.filterFindingsFor(result, TrafficLight.GREEN)).thenReturn(greenList);
	}

	@Test
	public void test_all_parameters_build_webdesignmode_false() {
		/* prepare */
		UUID uuid = UUID.randomUUID();
		
		when(scanResult.getInfo()).thenReturn("theInfo");
		when(scanResult.getJobUUID()).thenReturn(uuid);
		when(scanResult.getTrafficLight()).thenReturn("trafficlight1");
		
		builderToTest.webDesignMode=false;
		builderToTest.embeddedCSS="embeddedCssContent";
		
		
		/* execute */
		Map<String, Object> map = builderToTest.build(scanResult);
		
		/* test */
		assertEquals("theInfo", map.get("info"));
		assertSame(result, map.get("result"));
		assertSame(greenList, map.get("greenList"));
		assertSame(redList, map.get("redList"));
		assertSame(yellowList, map.get("yellowList"));
		assertEquals(false,map.get("isWebDesignMode"));
		assertEquals("embeddedCssContent",map.get("embeddedCSS"));
		assertNull(map.get("${includedCSSRef}"));
		
		assertEquals("trafficlight1", map.get("trafficlight"));
		assertEquals(uuid.toString(), map.get("jobuuid"));
		assertEquals(HIDE_LIGHT, map.get("styleRed"));
		assertEquals(HIDE_LIGHT, map.get("styleYellow"));
		assertEquals(HIDE_LIGHT, map.get("styleGreen"));
	}
	
	@Test
	public void test_all_parameters_build_webdesignmode_true() throws Exception {
		/* prepare */
		UUID uuid = UUID.randomUUID();
		
		when(scanResult.getInfo()).thenReturn("theInfo");
		when(scanResult.getJobUUID()).thenReturn(uuid);
		when(scanResult.getTrafficLight()).thenReturn("trafficlight1");
		builderToTest.webDesignMode=true;
		builderToTest.embeddedCSS="embeddedCssContent";
		Resource cssResource = mock(Resource.class);
		File expectedFile = ScanDomainTestFileSupport.getTestfileSupport().createFileFromRoot("sechub-scan/src/main/resources/templates/report/html/scanresult.css"); 
		when(cssResource.getFile()).thenReturn(expectedFile);
		builderToTest.cssResource=cssResource;
		
		/* execute */
		Map<String, Object> map = builderToTest.build(scanResult);
		
		/* test */
		assertEquals("theInfo", map.get("info"));
		assertSame(result, map.get("result"));
		assertSame(greenList, map.get("greenList"));
		assertSame(redList, map.get("redList"));
		assertSame(yellowList, map.get("yellowList"));
		assertEquals(true,map.get("isWebDesignMode"));
		
		// check css ref for webdesign mode
		assertNotNull(map.get("includedCSSRef"));
		String path = (String)map.get("includedCSSRef");
		File foundFile = new File(path);
		
		assertEquals(expectedFile.getCanonicalPath(),foundFile.getCanonicalPath());
		
		assertEquals("trafficlight1", map.get("trafficlight"));
		assertEquals(uuid.toString(), map.get("jobuuid"));
		assertEquals(HIDE_LIGHT, map.get("styleRed"));
		assertEquals(HIDE_LIGHT, map.get("styleYellow"));
		assertEquals(HIDE_LIGHT, map.get("styleGreen"));
	}
	
	@Test
	public void trafficlight_red_set_display_block__others_are_none() {
		when(scanResult.getTrafficLight()).thenReturn(TrafficLight.RED.name());
		
		Map<String, Object> map = builderToTest.build(scanResult);
		assertEquals(SHOW_LIGHT, map.get("styleRed"));
		assertEquals(HIDE_LIGHT, map.get("styleYellow"));
		assertEquals(HIDE_LIGHT, map.get("styleGreen"));
	}
	
	@Test
	public void trafficlight_yellow_set_display_block__others_are_none() {
		when(scanResult.getTrafficLight()).thenReturn(TrafficLight.YELLOW.name());
		
		Map<String, Object> map = builderToTest.build(scanResult);
		assertEquals(HIDE_LIGHT, map.get("styleRed"));
		assertEquals(SHOW_LIGHT, map.get("styleYellow"));
		assertEquals(HIDE_LIGHT, map.get("styleGreen"));
	}
	

	@Test
	public void trafficlight_green_set_display_block__others_are_none() {
		when(scanResult.getTrafficLight()).thenReturn(TrafficLight.GREEN.name());
		
		Map<String, Object> map = builderToTest.build(scanResult);
		assertEquals(HIDE_LIGHT, map.get("styleRed"));
		assertEquals(HIDE_LIGHT, map.get("styleYellow"));
		assertEquals(SHOW_LIGHT, map.get("styleGreen"));
	}

}
