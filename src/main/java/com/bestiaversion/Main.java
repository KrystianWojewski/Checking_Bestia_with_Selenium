package com.bestiaversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class Main {
    public static void main(String[] args) {

        String downloadDir = System.getenv("DOWNLOAD_DIR");

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("safebrowsing.enabled", true);
        prefs.put("download.default_directory", downloadDir);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);

        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);

        String actualVersion = getVersionFromFile();
        String isFilesDownloaded = "";

        driver.manage().window().maximize();
            driver.navigate().to("https://budzetjst.pl/pobieranie/instalacja/bestia/");
            String version = driver
                    .findElement(By.xpath("/html/body/div/main/div/div/div/article/div/div/div/div[1]/div/h3"))
                    .getText();
            version = version.split(" ")[2];

        try {
            if (!version.equals(actualVersion)) {
                CreateFile("BestiaVersion.txt");
                WriteToFile("BestiaVersion.txt", "Bestia version: " + version);

                System.out.println("New Bestia version is available: " + version);

                DevTools devTools = ((HasDevTools) driver).getDevTools();
                devTools.createSession();

                Map<String, Object> params = new HashMap<>();
                params.put("behavior", "allow");
                params.put("downloadPath", downloadDir);

                ((HasCdp) driver).executeCdpCommand("Page.setDownloadBehavior", params);
                
                WebElement downloadButton = driver.findElement(By.xpath("/html/body/div/main/div/div/div/article/div/div/div/div[1]/a"));
                String downloadUrl = downloadButton.getAttribute("href");
                DownloadFile(driver, downloadUrl); // Pobieranie instalacji

                driver.navigate().to("https://budzetjst.pl/pobieranie/aktualizacje/bestia/");
                downloadButton = driver.findElement(By.xpath("/html/body/div/main/div/div/div/article/div/div/div/div[1]/a"));
                downloadUrl = downloadButton.getAttribute("href");
                DownloadFile(driver, downloadUrl); // Pobieranie aktualizacji

                TimeUnit.SECONDS.sleep(90);

                File installationFile = new File(downloadDir + File.separator + "BestiaSetup_" + version + ".exe");
                File updateFile = new File(downloadDir + File.separator + "BestiaPatch_" + version + ".exe");

                if (installationFile.exists() && updateFile.exists()) {
                    isFilesDownloaded = "Files downloaded successfully";

                    installationFile.renameTo(new File(
                            downloadDir + "\\Instalacja" + File.separator + "BestiaSetup_" + version + ".exe"));
                    updateFile.renameTo(new File(
                            downloadDir + "\\Aktualizacja" + File.separator + "BestiaPatch_" + version + ".exe"));

                    System.out.println(isFilesDownloaded + "and moved to respective folders.");
                } else {
                    isFilesDownloaded = "Failed to download the files.";
                    System.out.println(isFilesDownloaded);
                }

                SendMail("Bestia version update", "New version of Bestia is available: " + version
                        + "\n" + "Location: " + downloadDir);

            } else {
                System.out.println("Bestia version is up to date: " + version);
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private static String getVersionFromFile() {
        String version = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("BestiaVersion.txt"));
            version = br.readLine().split(" ")[2];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    private static InternetAddress[] getRecipientsFromFile() {
        InternetAddress[] recipients = null;
        ArrayList<String> tmpRecipient = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("Recipients.txt"));
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                tmpRecipient.add(line);
                count++;
            }
            br.close();

            recipients = new InternetAddress[count];
            for (int i = 0; i < count; i++) {
                recipients[i] = new InternetAddress(tmpRecipient.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipients;
    }

    private static void WriteToFile(String fileName, String content) {
        try {
            FileWriter Writer = new FileWriter(fileName);
            Writer.write(content);
            Writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void CreateFile(String fileName) {
        try {
            File File = new File(fileName);
            if (File.createNewFile()) {
                System.out.println("File created: " + File.getName());
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void SendMail(String subject, String content) {
        String from = System.getenv("EMAIL_FROM");

        final String username = System.getenv("EMAIL_USERNAME");
        final String password = System.getenv("EMAIL_PASSWORD");

        String host = System.getenv("EMAIL_HOST");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        try {
            InternetAddress[] recipients = getRecipientsFromFile();

            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, recipients);
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

            System.out.println("Email sent successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void DownloadFile(WebDriver driver, String downloadUrl) throws IOException, InterruptedException {
        driver.get(downloadUrl);
    }
}