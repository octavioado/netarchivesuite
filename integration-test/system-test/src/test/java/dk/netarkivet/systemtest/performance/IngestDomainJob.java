package dk.netarkivet.systemtest.performance;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import dk.netarkivet.systemtest.TestLogger;
import dk.netarkivet.systemtest.environment.GUIApplicationManager;
import dk.netarkivet.systemtest.environment.TestController;
import dk.netarkivet.systemtest.page.PageHelper;

/**
* Job to ingest a domain list from a file on the local machine via the web interface. Although implemented as
* a LongRunningJob, this actually runs as a single browser operation so the job should already be completed when startJob()
 * returns.
*/
class IngestDomainJob extends GenericWebJob {
    protected final TestLogger log = new TestLogger(getClass());

    public IngestDomainJob(StressTest stressTest, WebDriver webDriver, Long maxTime) {
          super(stressTest, stressTest.testController, webDriver, 0L, 60L, maxTime, "Ingest Domain Job");
    }

    @Override void startJob() {
        String backupEnv = System.getProperty("systemtest.backupenv", "prod");
        TestController testController = stressTest.testController;
        GUIApplicationManager GUIApplicationManager = new GUIApplicationManager(testController);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        String baseUrl = testController.getGuiHost() + ":" + testController.getGuiPort();
        PageHelper.initialize(driver, baseUrl);
        GUIApplicationManager.waitForGUIToStart(60);
        stressTest.addFixture("Opening initial page " + baseUrl);
        File domainsFile = null;
        try {
            domainsFile = File.createTempFile("domains", "txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stressTest.addStep("Getting domain file", "The file should be downloaded");
        int returnCode = 0;
        try {
            Process p = Runtime.getRuntime().exec("scp test@kb-prod-udv-001.kb.dk:" + backupEnv +"-backup/domain.*.txt " + domainsFile.getAbsolutePath());
            returnCode = p.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals(returnCode, 0, "Return code from scp command is " + returnCode);
        assertTrue(domainsFile.length() > 100000L, "Domain file " + domainsFile.getAbsolutePath() + " is too short");
        stressTest.addStep("Ingesting domains from " + domainsFile.getAbsolutePath(),
                "Expect to see domain generation.");
        driver.findElement(By.linkText("Definitions")).click();
        driver.findElement(By.linkText("Create Domain")).click();
        List<WebElement> elements = driver.findElements(By.name("domainlist"));
        for (WebElement element: elements) {
            if (element.getAttribute("type").equals("file")) {
                element.sendKeys(domainsFile.getAbsolutePath());
            }
        }
        elements = driver.findElements(By.tagName("input"));
        for (WebElement element: elements) {
            if (element.getAttribute("type").equals("submit") && element.getAttribute("value").equals("Ingest")) {
                element.submit();
            }
        }
    }

    @Override boolean isStarted() {
         return true;
    }

    @Override boolean isFinished() {
        return driver.getPageSource().contains("Ingesting done");
    }

    @Override String getProgress() {
        return "";
    }
}
