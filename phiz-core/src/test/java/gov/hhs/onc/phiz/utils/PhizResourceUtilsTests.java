package gov.hhs.onc.phiz.utils;

import gov.hhs.onc.phiz.test.impl.AbstractPhizTests;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "phiz.test.utils.all", "phiz.test.utils.resource" }, enabled = false)
public class PhizResourceUtilsTests extends AbstractPhizTests {
    @Value("${phiz.test.utils.resource.resource.1.path}")
    private String testResourcePath1;

    @Value("${phiz.test.utils.resource.resource.1.file.path}")
    private Resource testFileResource1;

    @Value("${phiz.test.utils.resource.resource.1.jar.path}")
    private Resource testJarResource1;

    @Value("${phiz.test.utils.resource.resource.2.path}")
    private String testResourcePath2;

    @Value("${phiz.test.utils.resource.resource.2.file.path}")
    private Resource testFileResource2;

    @Value("${phiz.test.utils.resource.resource.2.jar.path}")
    private Resource testJarResource2;

    @Value("${phiz.test.utils.resource.resource.3.path}")
    private String testResourcePath3;

    @Value("${phiz.test.utils.resource.resource.3.file.path}")
    private Resource testFileResource3;

    @Value("${phiz.test.utils.resource.resource.3.jar.path}")
    private Resource testJarResource3;

    @Test(dependsOnMethods = { "testExtractFilePath" })
    public void testSortByLocation() throws Exception {
        Resource[] resourcesExpected = ArrayUtils.toArray(this.testJarResource1, this.testFileResource1, this.testJarResource2, this.testFileResource2,
            this.testJarResource3, this.testFileResource3), resources = ArrayUtils.clone(resourcesExpected);

        // noinspection ConstantConditions
        List<Resource> resourceList = Arrays.asList(resources);
        Collections.shuffle(resourceList);
        resourceList.sort(PhizResourceUtils.LOC_COMPARATOR);

        Assert.assertEquals((resources = resourceList.toArray(new Resource[resourceList.size()])), resourcesExpected,
            String.format("Unable to sort resources by overridden locations: expected=[%s], actual=[%s]", StringUtils.join(resourcesExpected, "; "),
                StringUtils.join(resources, "; ")));
    }

    @Test(dependsOnMethods = { "testExtractMetaInfPath" })
    public void testExtractFilePath() throws Exception {
        Assert.assertEquals(PhizResourceUtils.extractFilePath(PhizResourceUtils.extractPath(this.testJarResource1)), this.testResourcePath1);
    }

    @Test
    public void testExtractMetaInfPath() throws Exception {
        Assert.assertEquals(PhizResourceUtils.extractPath(this.testFileResource1, true), PhizResourceUtils.extractPath(this.testJarResource1, true));
    }
}
