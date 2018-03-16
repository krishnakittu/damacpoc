package com.damacpoc.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.damacpoc.util.AppUtil;
import com.damacpoc.util.PersistenceFactory;
import com.dmacpoc.entities.MailObject;
import com.sun.mail.util.MailSSLSocketFactory;

public class EmailReci {
	public static final Logger logger = LoggerFactory
			.getLogger(EmailReci.class);
	private static int myImageIndex = 0;
	public   void receiveMails() throws MessagingException, IOException  {
		Properties props = new Properties();
		String protocol="imaps";
		props.setProperty("mail." + protocol + ".host", "pop.gmail.com");
		props.setProperty("mail." + protocol + ".port","993");
		props.setProperty("mail." + protocol + ".connectiontimeout", "10000");
		props.setProperty("mail." + protocol + ".timeout", "10000");
		if(protocol.equalsIgnoreCase("imap")|| protocol.equalsIgnoreCase("imaps")
				|| protocol.equalsIgnoreCase("pop3")|| protocol.equalsIgnoreCase("pop3s")){
			MailSSLSocketFactory sf=null;
			try {
				sf = new MailSSLSocketFactory();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sf.setTrustAllHosts(true);
			props.put("mail."+ protocol +".starttls.enable", "true");
			props.put("mail."+ protocol +".ssl.socketFactory", sf);
		}
		MailAuthenticator passwordAuthentication = new MailAuthenticator(
				"itsmerohankovid@gmail.com", "surikittu12!");
		Session session = Session
				.getInstance(props, passwordAuthentication);
		Store emailStore=null;
		Folder folder=null;
		emailStore = session.getStore(protocol);
		emailStore.connect();
		folder = emailStore.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
			// get unread messages only
			FlagTerm ft = new FlagTerm(new Flags(
					Flags.Flag.SEEN), false);
			Message[] messages = folder.search(ft);
			if (messages != null && messages.length > 0) {
				logger.info("Total " + messages.length
						+ " Unread messages for BA \""
						+ "\"");
				myImageIndex = 0;
				for (int messageCount = 0; messageCount < messages.length; messageCount++) {
					Message aMessage = messages[messageCount];
					Enumeration<?> headers = aMessage
							.getAllHeaders();
					StringBuffer emailHeaderLog = new StringBuffer("");
					String messageID="";
					String inreplyTo="";
					String references="";
					while (headers.hasMoreElements()) {
						Header h = (Header) headers
								.nextElement();
						if(h.getName().equals("Message-ID")){
							messageID=h.getValue();
						}else if(h.getName().equals("In-Reply-To")){
							inreplyTo=h.getValue();
						}else if (h.getName().equals("References"))
							references=h.getValue();
					}
					
					logger.info("\n Message Headers : \n"
							+ emailHeaderLog.toString());
					String subject = aMessage.getSubject();

					/*
					 * parses recipient address in To field
					 */
					String toAddresses = AppUtil.getApplicationProperties("tmpDir");;
					List<String> ToList = new ArrayList<String>();
					Address[] arrayTo = aMessage
							.getRecipients(Message.RecipientType.TO);
					if (arrayTo != null) {
						for (int toCount = 0; toCount < arrayTo.length; toCount++) {
							toAddresses += arrayTo[toCount]
									.toString() + ", ";
							ToList.add(parseEmailId(
									arrayTo[toCount].toString())
									.toLowerCase());
						}
					}
					
					if (toAddresses.length() > 1) {
						toAddresses = toAddresses.substring(0,
								toAddresses.length() - 2);
					}
					logger.info("toAddresses = " + toAddresses);

					/*
					 * parses recipient addresses in CC field
					 */
					String ccAddresses = AppUtil.getApplicationProperties("tmpDir");;
					List<String> ccList = new ArrayList<String>();
					Address[] arrayCC = aMessage
							.getRecipients(Message.RecipientType.CC);
					
					if (arrayCC != null) {
						for (int ccCount = 0; ccCount < arrayCC.length; ccCount++) {
							ccAddresses += arrayCC[ccCount]
									.toString() + ", ";
							ccList.add(parseEmailId(
									arrayCC[ccCount].toString())
									.toLowerCase());
						}
					}
					logger.info("ccAddresses = " + ccAddresses);
					String textMessage = "";
					String attachFiles = "";
					String description = "";
					StringBuffer log = new StringBuffer();
					// Integer myImageIndex = new Integer(0);
					String contentType = aMessage
							.getContentType();
					if (contentType.contains("text/plain")
							|| contentType
									.contains("text/html")) {
						Object content = aMessage.getContent();
						if (content != null) {
							String contentString = content
									.toString();
							if (aMessage
									.isMimeType("text/html") == true) {
								description = description
										.concat(contentString);
							}
							textMessage = textMessage
									.concat(contentString);
						}

					}else if (contentType
							.contains("multipart")) {
						Multipart multiPart = (Multipart) aMessage
								.getContent();
						int numberOfParts = multiPart
								.getCount();

						for (int partCount = 0; partCount < numberOfParts; partCount++) {
							BodyPart part = multiPart
									.getBodyPart(partCount);
							logger.info(part.getDisposition());
							logger.info(part.getContentType());
							if (Part.ATTACHMENT
									.equalsIgnoreCase(part
											.getDisposition())) {
								attachFiles += part
										.getFileName() + ", ";
								
							} else if (part.getContentType()
									.toLowerCase()
									.startsWith("multipart/")) {
								String[] multipartContents = processMultipart(part);
								if (multipartContents != null) {
									if (multipartContents.length == 2) {
										if (multipartContents[0] != null) {
											description = description
													.concat(multipartContents[0]);
											textMessage = textMessage
													.concat(multipartContents[0]);
										}

										if (multipartContents[1] != null
												&& !description
														.contains(multipartContents[1])) {
											description = description
													.concat(multipartContents[1]);
										}
									}
								}

							} else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) {

				logger.info("Inline part comes needs to proceess");
				description = handleInlineAttachment(
						part, description);
				logger.info("----------------------------------------------------------");

			}else {
				Object partContent = part
						.getContent();
				if (partContent != null) {
					String contentString = partContent
							.toString();
					if (part.isMimeType("text/html") == true
							&& !description
									.contains(contentString)) {
						description = description
								.concat(contentString);

					}
					textMessage = textMessage
							.concat(contentString);
				}
			}
		}if (attachFiles.length() > 1) {
			attachFiles = attachFiles
					.substring(0, attachFiles
							.length() - 2);
		}
	}
				
				//todo
					
					myImageIndex = 0;
					log.append("\n\nTextMessage:\n\n" + textMessage);
					log.append("\n\nAttachment String:\n\n" + attachFiles);
					log.append("\n\nDescription before parsing:\n\n" + description);
					log.append("\n\nDescription after parsing:\n\n" + description);
					MailObject globalobject = null;
					if (inreplyTo.isEmpty()) {
						globalobject = new MailObject(ToList.get(0), ccList.toString(),
								parseEmailId(aMessage.getFrom()[0].toString()), subject, parseEmailId(messageID), -1);
						PersistenceManager pm = PersistenceFactory
								.getPersistenceManager(0);
						Transaction tx = pm.currentTransaction();
						try {
							tx.begin();
							globalobject = pm.detachCopy(createPreFilledUser(pm, globalobject));
							tx.commit();
						} finally {
							// rollback in case of errors
							if (tx.isActive())
								tx.rollback();
							pm.close();
						}
					} else {
						PersistenceManager pm = PersistenceFactory
								.getPersistenceManager(0);
						Query q = pm.newQuery(MailObject.class);
						q.setFilter("this.messageId == messageId");
						q.declareParameters("String messageId");
						String messageId = parseEmailId(inreplyTo);
						// q.setUnique(true);
						List<MailObject> mo = (List<MailObject>) q.execute(messageId);
						pm.close();
						if (mo != null) {
							globalobject = new MailObject(ToList.get(0), ccList.toString(),
									parseEmailId(aMessage.getFrom()[0].toString()), subject, parseEmailId(messageID),
									(int) mo.get(0).getId());
							pm = PersistenceFactory
									.getPersistenceManager(0);
							Transaction tx = pm.currentTransaction();
							try {
								tx.begin();
								globalobject = pm.detachCopy(createPreFilledUser(pm, globalobject));
								tx.commit();
							} finally {
								// rollback in case of errors
								if (tx.isActive())
									tx.rollback();
								pm.close();
							}
						} else {
							// try with references
							List<String> str = Arrays.asList(references.replace("\r\n", " ").split(" "));
							Collections.reverse(str);
							for (String messageId1 : str) {
								pm = PersistenceFactory
										.getPersistenceManager(0);
								q = pm.newQuery(MailObject.class);
								q.setFilter("messageId == messageId");
								q.declareParameters("String messageId");
								// q.setUnique(true);
								messageId = parseEmailId(messageId);
								mo = (List<MailObject>) q.execute(messageId);
								pm.close();
								if (mo != null) {
									globalobject = new MailObject(ToList.get(0), ccList.toString(),
											parseEmailId(aMessage.getFrom()[0].toString()), subject, parseEmailId(messageID),
											(int) mo.get(0).getId());
									pm = PersistenceFactory
											.getPersistenceManager(0);
									Transaction tx = pm.currentTransaction();
									try {
										tx.begin();
										globalobject = pm.detachCopy(createPreFilledUser(pm, globalobject));
										tx.commit();
									} finally {
										// rollback in case of errors
										if (tx.isActive())
											tx.rollback();
										pm.close();
									}
								}
							}
						}
					}
					String basePath = AppUtil.getApplicationProperties("tmpDir");;
					File fs = new File("D:\\poc\\" + globalobject.getId());
					if (!fs.exists())
						fs.mkdir();
//					if (fileDataList != null) {
//						for (FileData eachFile : fileDataList) {
//							java.nio.file.Path movefrom = FileSystems.getDefault().getPath(basePath + eachFile.getValue());
//							java.nio.file.Path target = FileSystems.getDefault()
//									.getPath("D:\\tbits\\" + globalobject.getId() + "\\" + eachFile.getFileDisplayName());
//							Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
//						}
//					}
					FileOutputStream fse = new FileOutputStream(
							"D:\\poc\\" + globalobject.getId() + File.separator + globalobject.getId() + ".txt");
					aMessage.writeTo(fse);
				
				
				}}}
				
				
				private static String handleInlineAttachment(BodyPart part,
						String description) throws IOException, MessagingException {
					String location =AppUtil.getApplicationProperties("tmpDir"); //AppUtil.getAppProperty("tmpDir");
					if (!location.endsWith("/")) {
						location = location.concat("/");
					}
					String fullImageLocation = location + ""//generateRandomReportName()
							+ part.getFileName().replaceAll("[^a-zA-Z0-9._]", "_");
					FileOutputStream fos = new FileOutputStream(fullImageLocation);
					int i = 0;
					InputStream ais = part.getInputStream();
					while ((i = ais.read()) != -1) {
						fos.write(i);
					}

					fos.close();
					File file = new File(fullImageLocation);
					// Handled only 2MB Size
					if ((((file.length() / 1024) / 1024) / 1024) > 2)
						return description;
					FileInputStream imageInFile = new FileInputStream(file);
					byte imageData[] = new byte[(int) file.length()];
					imageInFile.read(imageData);
					String imageDataString = encodeImage(imageData);
					String copyofdescription = description;
					if (copyofdescription.indexOf("<img") >= 0) {
						// inline image found
						int index = part.getContentType().toLowerCase().indexOf("image/");
						int indexofImageLocation = copyofdescription.indexOf("<img",
								myImageIndex);
						int srcLocation = copyofdescription.indexOf("src=\"",
								indexofImageLocation);
						String cidPart = copyofdescription.substring(srcLocation + 5,
								copyofdescription.indexOf("\"", srcLocation + 5));
						System.out.println(cidPart);
						String base64String = "data:"
								+ part.getContentType().substring(index,
										part.getContentType().indexOf(";")) + ";base64,"
								+ imageDataString;
						;
						base64String = base64String.replaceAll("-", "+");
						base64String = base64String.replaceAll("_", "/");
						description = description.replace(cidPart, base64String);
						myImageIndex = srcLocation + base64String.length();
					}
					imageInFile.close();
					return description;
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
	
	public static String[] processMultipart(BodyPart bodyPart) {
		try {
			/*
			 * Following is array of length = 2 string at index = 0 stores plain
			 * text part string at index = 1 stores html part
			 */
			String[] multipartContents = new String[2];

			String htmlContent = "";
			String textContent = "";

			Multipart mp = (Multipart) bodyPart.getContent();

			int numberOfParts = mp.getCount();

			for (int i = 0; i < numberOfParts; i++) {
				BodyPart part = mp.getBodyPart(i);
				logger.info(part.getContentType());

				if (part.getContentType().toLowerCase()
						.startsWith("multipart/")) {
					String[] tempMultipartContents = processMultipart(part);
					if (tempMultipartContents != null) {
						if (tempMultipartContents.length == 2) {
							if (tempMultipartContents[0] != null) {
								textContent = textContent
										.concat(tempMultipartContents[0]);
							}

							if (tempMultipartContents[1] != null) {
								htmlContent = htmlContent
										.concat(tempMultipartContents[1]);
							}
						}
					}

				} else if (Part.ATTACHMENT.equalsIgnoreCase(part
						.getDisposition())) {
					continue;

				} else {
					Object partContent = part.getContent();
					if (partContent != null) {
						String contentString = partContent.toString();
						if (part.isMimeType("text/html") == true) {
							htmlContent = htmlContent.concat(contentString);
						}

						textContent = textContent.concat(contentString);
					}
				}
			}

			multipartContents[0] = textContent;
			multipartContents[1] = htmlContent;

			return multipartContents;

		} catch (Exception e) {
			logger.info("Exception : ", e);
		}

		return null;
	}
	
	public static String encodeImage(byte[] imageByteArray) {

		return Base64.encodeBase64URLSafeString(imageByteArray);
	}
	
	private static MailObject createPreFilledUser(PersistenceManager pm,
			MailObject ob) {
		return pm.makePersistent(ob);
	}

}
