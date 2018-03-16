package com.damacpoc.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.damacpoc.util.AppUtil;
import com.damacpoc.util.PersistenceFactory;
import com.damacpoc.util.ServiceURLConstant;
import com.dmacpoc.entities.MailObject;

@Path(ServiceURLConstant.PATH_URL)
public class RestService {

	private static Logger logger = LoggerFactory.getLogger(RestService.class);

	@Path(ServiceURLConstant.GET_DAMAC_POC)
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public static Response getDamacPOC(@QueryParam("clientId") long clientId, @FormDataParam("From") String from,
			@FormDataParam("To") String to, @FormDataParam("Subject") String subject,
			@FormDataParam("Description") String description, @FormDataParam("Cc") String cc,
			@FormDataParam("File") InputStream uploadedInputStream1,
			@FormDataParam("File") FormDataContentDisposition fileDetail1) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.sendgrid.net");
		props.put("mail.smtp.port", "25");
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", "true");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.login.id", "krohit");
		props.put("mail.smtp.login.password", "qwertyuiop@123");
		props.put("mail.smtp.ssl.trust", "*");

		try {
			final String userName = props.getProperty("mail.smtp.login.id");
			final String password = props.getProperty("mail.smtp.login.password");
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
			});
			logger.info("Properties: " + props);
			int a = 0;
			Multipart mp = new MimeMultipart("mixed");
			MimeBodyPart mbp1 = new MimeBodyPart();

			boolean inlineimage = false;
			String encodedstring = "";
			String extension = "";
			int numberofInlilneImages = 0;
			int fromIndex = 0;
			while (description.indexOf("<img", fromIndex) >= 0) {
				if (description.contains("src=\"data:image") && description.contains("base64,")) { // Hard
					// Coded
					inlineimage = true;
					int index = description.indexOf("<img", fromIndex);
					int nextIndex = description.indexOf("src", index);
					int quoteStartIndex = description.indexOf("\"", nextIndex);
					int quoteEndIndex = description.indexOf("\"", quoteStartIndex + 1);
					String replacable = description.substring(quoteStartIndex + 1, quoteEndIndex);
					int base64StartIndex = description.indexOf("base64,", nextIndex) + "base64,".length();
					int base64EndIndex = description.indexOf("\"", base64StartIndex + 1);
					encodedstring = description.substring(base64StartIndex, base64EndIndex);
					int extensionstratIndex = description.indexOf("data:image/", nextIndex);
					int extensionendIndex = description.indexOf(";", extensionstratIndex + "data:image/".length());
					extension = description.substring(extensionstratIndex + "data:image/".length(), extensionendIndex);
					description = (description.replace(replacable, "cid:image" + numberofInlilneImages));
					fromIndex = description.indexOf("cid:image" + numberofInlilneImages);
					numberofInlilneImages++;
				} else {
					break;
				}

			}
			mbp1.setContent(description, "text/html; charset=\"utf-8\"");
			mbp1.setHeader("MIME-Version", "1.0");
			mbp1.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
			mbp1.setHeader("Content-Transfer-Encoding", "quoted-printable");
			mp.addBodyPart(mbp1, a);
			a++;
			if (inlineimage) {
				for (int i = 0; i < numberofInlilneImages; i++) {
					BodyPart messageBodyPart = new MimeBodyPart();
					byte[] btDataFile = Base64.decodeBase64(encodedstring);
					File of = File.createTempFile("image" + Calendar.getInstance().getTimeInMillis(), "." + extension);
					FileOutputStream osf = new FileOutputStream(of);
					osf.write(btDataFile);
					osf.flush();
					osf.close();
					FileDataSource fds1 = new FileDataSource(of.getAbsolutePath());
					messageBodyPart.setDataHandler(new DataHandler(fds1));
					messageBodyPart.setHeader("Content-ID", "<image" + i + ">");
					mp.addBodyPart(messageBodyPart, a);
					a++;
					of.deleteOnExit();
				}
			}
			String localtion = createFileOnTempFolderFromRequest(uploadedInputStream1, fileDetail1);
			addAttachment(mp, uploadedInputStream1, fileDetail1);
			MimeMessage msg = new MimeMessage(session);
			List<String> tos = Arrays.asList(to.split(","));
			Address[] toAddresses = new Address[tos.size()];
			int i = 0;
			for (String str : tos) {
				toAddresses[i++] = new InternetAddress(str);
			}
			i = 0;
			if (!cc.isEmpty()) {
				List<String> ccs = Arrays.asList(cc.split(","));
				Address[] ccAddresses = new Address[ccs.size()];

				for (String str : ccs) {
					ccAddresses[i++] = new InternetAddress(str);
				}
				msg.setRecipients(Message.RecipientType.CC, ccAddresses);
			}

			msg.setRecipients(Message.RecipientType.TO, toAddresses);

			msg.setSubject(subject);
			msg.setFrom(new InternetAddress(from, from));
			Address[] addr = new Address[1];
			addr[0] = new InternetAddress("itsmerohankovid@gmail.com", "Support");
			msg.setReplyTo(addr);
			msg.setReplyTo(addr);
			msg.setContent(mp);
			Transport.send(msg);

			MailObject ob = new MailObject(to, cc, from, subject, parseEmailId(msg.getMessageID()), -1);
			PersistenceManager pm = PersistenceFactory.getPersistenceManager(clientId);
			Transaction tx = pm.currentTransaction();
			try {
				tx.begin();
				MailObject createdSimpleRulePlugin = pm.detachCopy(createPreFilledUser(pm, ob));
				tx.commit();
			} finally {
				// rollback in case of errors
				if (tx.isActive())
					tx.rollback();
				pm.close();
			}
			File f = new File("D:\\poc\\" + ob.getId());
			if (!f.exists())
				f.mkdir();
			FileOutputStream fos = new FileOutputStream("D:\\poc\\" + ob.getId() + "\\" + ob.getId() + ".txt");
			msg.writeTo(fos);
			if (localtion != null) {
				java.nio.file.Path movefrom = FileSystems.getDefault().getPath(localtion);
				File f1 = new File(localtion);

				java.nio.file.Path target = FileSystems.getDefault()
						.getPath("D:\\poc\\" + ob.getId() + "\\" + f1.getName());
				Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.status(Response.Status.OK).build();
	}

	public static String createFileOnTempFolderFromRequest(InputStream inputStream,
			FormDataContentDisposition fileDetail) throws Exception {
		logger.info("Creating file on temp folder.");
		String actualFileLocation = AppUtil.getApplicationProperties("actualFileLocation");
		;
		if (inputStream != null) {
			String fileLocation = fileDetail.getFileName();
			java.nio.file.Path path = Paths.get(fileLocation);
			String fileName = path.getFileName().toString();
			String rdcaTempPath = AppUtil.getApplicationProperties("tmpDir");
			actualFileLocation = rdcaTempPath + File.separator + FilenameUtils.removeExtension(fileName) + "_"
					+ new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date()) + "."
					+ FilenameUtils.getExtension(fileName);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(new File(actualFileLocation));
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = inputStream.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
			} catch (Exception e) {
				logger.error("Error while file" + actualFileLocation + " downloading." + e);
				actualFileLocation = "";
			} finally {
				out.flush();
				out.close();
				System.gc();
			}
		} else {
			logger.error("Error because of null InputStream.");
		}
		return actualFileLocation;
	}

	public static String parseEmailId(String mailIdWithName) {
		String mailId = mailIdWithName;
		int startingIndex = mailIdWithName.indexOf("<");
		int endingIndex = mailIdWithName.indexOf(">");
		if (startingIndex != -1 && endingIndex != -1) {
			mailId = mailIdWithName.substring(startingIndex + 1, endingIndex);
		}
		return mailId.trim();
	}

	private static MailObject createPreFilledUser(PersistenceManager pm, MailObject ob) {
		return pm.makePersistent(ob);
	}

	private static void addAttachment(Multipart multipart, InputStream uploadedInputStream1,
			FormDataContentDisposition fileDetail1) throws Exception {
		String path = AppUtil.getApplicationProperties("tmpDir");

		String localtion = createFileOnTempFolderFromRequest(uploadedInputStream1, fileDetail1);
		File f = new File(localtion);
		if (uploadedInputStream1 != null) {
			byte[] buf = new byte[1024];
			long length = 0;

			File file = File.createTempFile(f.getName(), null, new File(path));
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			while ((uploadedInputStream1 != null) && ((length = uploadedInputStream1.read(buf)) != -1)) {
				fileOutputStream.write(buf, 0, (int) length);
			}
			fileOutputStream.flush();
			uploadedInputStream1.close();
			fileOutputStream.close();

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(file.getAbsolutePath());
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(f.getName());
			multipart.addBodyPart(messageBodyPart);

			file.deleteOnExit();

		}

	}

}
