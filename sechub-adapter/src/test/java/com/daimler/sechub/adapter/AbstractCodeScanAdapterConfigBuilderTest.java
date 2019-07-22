// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.junit.Test;

public class AbstractCodeScanAdapterConfigBuilderTest {

	@Test
	public void settingSourceFoldersNullResultsInNullTargetString() {
		TestCodeScanAdapterConfig x = new TestAbstractCodeScanAdapterConfigBuilder()
				.build();
		/* test */
		assertEquals(null, x.getTargetAsString());
	}
	
	@Test
	public void settingSourceFoldersResultsInTargetString() {
		TestCodeScanAdapterConfig x = new TestAbstractCodeScanAdapterConfigBuilder()
				.setFileSystemSourceFolders(new LinkedHashSet<>(Arrays.asList("src/java/", "src/groovy"))).build();
		/* test */
		assertEquals("src/java/;src/groovy", x.getTargetAsString());
	}

	
	private class TestAbstractCodeScanAdapterConfigBuilder extends
			AbstractCodeScanAdapterConfigBuilder<TestAbstractCodeScanAdapterConfigBuilder, TestCodeScanAdapterConfig> {

		@Override
		protected void customBuild(TestCodeScanAdapterConfig config) {

		}

		@Override
		protected TestCodeScanAdapterConfig buildInitialConfig() {
			return new TestCodeScanAdapterConfig();
		}

		@Override
		protected void customValidate() {

		}

	}

	private class TestCodeScanAdapterConfig extends AbstractCodeScanAdapterConfig {

	}
}
