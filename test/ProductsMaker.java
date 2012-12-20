import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.junit.Test;

public class ProductsMaker {

	@Test
	public void getImages() {
		
		String address = "http://www.impawards.com/2010/std.html";
		Integer count = 10;
		String outputDir = "data";
		
		// obrazki
		try {

			URL url = new URL(address);
			URLConnection uc = url.openConnection();
			InputStream is = uc.getInputStream();
			String prefix = address.substring(0, address.lastIndexOf('/') + 1);

			// jericho
			Source source = new Source(is);
			List<Element> divs = source.getAllElements(HTMLElementName.DIV);
			Element content = null;
			for (Element div : divs) {
				String styleClass = div.getAttributeValue("class");
				if (styleClass != null && styleClass.equals("content")) {
					content = div;
					break;
				}
			}

			if (content != null) {

				List<Element> trs = content.getAllElements(HTMLElementName.TR);
				System.out.println(trs.size() + " nodes found.");
				Integer i = 0;
				Collections.shuffle(trs); // pomieszaj :)
				for (Element tr : trs) {
					List<Element> list = tr.getAllElements(HTMLElementName.IMG);

					Element tdWithTitle = tr.getFirstElement(HTMLElementName.TD);
					Element font = tdWithTitle.getFirstElement();
					String name = new TextExtractor(font.getContent()).toString();

					Image im = new Image();
					im.setName(name);
					
					System.out.println("Output path: "+new File(outputDir).getAbsolutePath());
					
					File d = new File(outputDir, name);
					boolean mkdir = d.mkdirs();
					System.out.println(mkdir  ? "Directory '"+d.getAbsolutePath()+"' created" : "Directory '"+d.getAbsolutePath()+"' NOT created");
					
					// dodaj obrazki do produktu
					for (Element img : list) {
						String imageUrl = prefix + img.getAttributeValue("src");
						imageUrl = imageUrl.replaceFirst("thumbs/imp_", "posters/");
						
						if(mkdir) {
							urlToFile(imageUrl, new FileOutputStream(new File(d, imageUrl.substring(imageUrl.lastIndexOf('/')+1))));
						}
						
						/*
						byte[] data = urlToBytes(imageUrl);
						*/
						im.setUrl(imageUrl);
						im.setFilename(imageUrl.substring(imageUrl.lastIndexOf('/')+1));							
						System.out.println("Image " + imageUrl + " (name: "+ name +") added.");
					}
					
					i++;
					if(count != null && i == count) {
						break;
					}
					
				}
				System.out.println(i + " products added.");

				/*
				Integer i = 0;
				//if(false)
				Collections.shuffle(list); // pomieszaj :)
				for (Element img : list) {

					String imageUrl = prefix + img.getAttributeValue("src");
					imageUrl = imageUrl.replaceFirst("thumbs/imp_", "posters/");
					String name = imageUrl.substring(imageUrl.lastIndexOf('/')+1);
					name = name.substring(0, name.lastIndexOf('.')).replaceAll("_ver[0-9].*$", "").replaceAll("_", " ");
					byte[] data = urlToBytes(imageUrl);

					Category category = randomCategory();
					Product product = new Product(UndefinedUtils.randomWord(), admin, category);
					product.setAddDate(new Date());
					product.setModDate(new Date());
					product.setPrice((float)(Math.random() * 50));
					product.setDvdPrice((float)(Math.random() * 50));
					product.setName(name);
					product.setDescription("When his idyllic life is threatened by a high-tech assassin, former black-ops agent Frank Moses reassembles his old team in a last ditch effort to survive. Frank (Bruce Willis), Joe (Morgan Freeman), Marvin (John Malkovich) and Victoria (Helen Mirren) used to be the CIAs top agents but the secrets they know just made them the Agencys top targets. Now framed for assassination, they must use all of their collective cunning, experience and teamwork to stay one step ahead of their deadly pursuers and stay alive. To stop the operation, the team embarks on an impossible, cross-country mission to break into the top-secret CIA headquarters, where they will uncover one of the biggest conspiracies and cover-ups in government history.");
					getProductDAO().add(product);

					Image image = new Image(data);
					image.setProduct(product);
					getImageDAO().add(image);

					System.out.println("Image " + imageUrl + " (name: "+ name +") added.");
					i++;
					if(count != null && i == count) {
						break;
					}

				}
				System.out.println(i + " products added.");
				*/
			}
			
			is.close();
			System.out.println("END");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void urlToFile(String stringUrl, FileOutputStream out) {
		InputStream is = null;
		try {
			URL url = new URL(stringUrl);
			is = url.openStream();
			byte[] byteChunk = new byte[4096];
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				out.write(byteChunk, 0, n);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}
	
	private byte[] urlToBytes(String stringUrl) {
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		InputStream is = null;

		try {
			URL url = new URL(stringUrl);
			is = url.openStream();
			byte[] byteChunk = new byte[4096];
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				bais.write(byteChunk, 0, n);
			}
			is.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bais.toByteArray();
	}

}

class Image {
	private String filename;
	private String name;
	private String url;

	
	public String getUrl() {
		return url;
	}

	
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	
	public String getFilename() {
		return filename;
	}
	
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
}