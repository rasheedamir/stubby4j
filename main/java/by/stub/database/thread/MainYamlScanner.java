package by.stub.database.thread;

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.yaml.YamlParser;

import java.io.File;
import java.util.Date;

import static by.stub.utils.FileUtils.BR;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 8:01 AM
 */
public final class MainYamlScanner implements Runnable {

   private final long sleepTime;
   private final StubbedDataManager stubbedDataManager;

   public MainYamlScanner(final StubbedDataManager stubbedDataManager, final long sleepTime) {
      this.sleepTime = sleepTime;
      this.stubbedDataManager = stubbedDataManager;
      ANSITerminal.status(String.format("Main YAML scan enabled, watching %s", stubbedDataManager.getYamlCanonicalPath()));
   }

   @Override
   public void run() {

      try {
         final File dataYaml = stubbedDataManager.getDataYaml();
         long mainYamlLastModified = dataYaml.lastModified();

         while (!Thread.currentThread().isInterrupted()) {

            Thread.sleep(sleepTime);

            final long currentFileModified = dataYaml.lastModified();
            if (mainYamlLastModified >= currentFileModified) {
               continue;
            }

            ANSITerminal.info(String.format("%sMain YAML scan detected change in %s%s", BR, stubbedDataManager.getYamlCanonicalPath(), BR));

            try {
               mainYamlLastModified = currentFileModified;
               stubbedDataManager.refreshStubbedData(new YamlParser());
               ANSITerminal.ok(String.format("%sSuccessfully performed live refresh of main YAML file from: %s on [" + new Date().toString().trim() + "]%s",
                  BR,
                  dataYaml.getAbsolutePath(),
                  BR));
            } catch (final Exception ex) {
               ANSITerminal.error("Could not refresh YAML file: " + ex.toString());
               ANSITerminal.warn(String.format("YAML refresh aborted, in-memory stubs remain untouched"));
            }
         }

      } catch (final Exception ex) {
         ex.printStackTrace();
         ANSITerminal.error("Could not perform live YAML scan: " + ex.toString());
      }
   }
}