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
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("safebrowsing.enabled", true);
        prefs.put("profile.default_content_setting_values.automatic_downloads", 1);
        prefs.put("download.default_directory", downloadDir);
        prefs.put("savefile.default_directory", downloadDir);

        options.setExperimentalOption("prefs", prefs);

        String actualVersion = getVersionFromFile();
        WebDriver driver = new ChromeDriver(options);
        String isFilesDownloaded = "";

        try {
            driver.manage().window().maximize();
            driver.navigate().to("https://budzetjst.pl/pobieranie/instalacja/bestia/");
            String version = driver
                    .findElement(By.xpath("/html/body/div/main/div/div/div/article/div/div/div/div[1]/div/h3"))
                    .getText();
            version = version.split(" ")[2];

            if (!version.equals(actualVersion)) {
                CreateFile("BestiaVersion.txt");
                WriteToFile("BestiaVersion.txt", "Bestia version: " + version);

                System.out.println("New Bestia version is available: " + version);

                DownloadFile(driver); // Pobieranie instalacji

                driver.navigate().to("https://budzetjst.pl/pobieranie/aktualizacje/bestia/");
                DownloadFile(driver); // Pobieranie aktualizacji

                TimeUnit.SECONDS.sleep(90);

                File installationFile = new File(downloadDir + File.separator + "BestiaSetup_" + version + ".exe");
                File updateFile = new File(downloadDir + File.separator + "BestiaPatch_" + version + ".exe");

                if (installationFile.exists() && updateFile.exists()) {
                    isFilesDownloaded = "Files downloaded successfully";

                    installationFile.renameTo(new File(downloadDir + "\\Instalacja" + File.separator + "BestiaSetup_" + version + ".exe"));
                    updateFile.renameTo(new File(downloadDir + "\\Aktualizacja" + File.separator + "BestiaPatch_" + version + ".exe"));

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
    private static void DownloadFile(WebDriver driver){
        driver.findElement(By.xpath("/html/body/div/main/div/div/div/article/div/div/div/div[1]/a")).click();
    }
}