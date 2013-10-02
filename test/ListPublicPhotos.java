import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import com.google.gdata.client.Query;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.data.media.mediarss.MediaGroup;

/**
 * picasa uploader
 * 
 * usage: java -jar picasa-uploader.jar [options] <dir1|file1> <dir2|file2>...
 *  -d <arg>   album description
 *  -h         help
 *  -l         list albums
 *  -p <arg>   password [REQUIRED]
 *  -t <arg>   album title
 *  -u <arg>   user [REQUIRED]
 *  -v         be verbose
 *         
 * @author marioosh
 *
 */
public class ListPublicPhotos {

	Logger log = Logger.getLogger(ListPublicPhotos.class);
	
	private static String[] args;
	private static final String API_PREFIX = "https://picasaweb.google.com/data/feed/api/user/";
	static final String API_URL =            "https://picasaweb.google.com/data/entry/api/user/default";
	static final String API_FEED_URL =       "https://picasaweb.google.com/data/feed/api/user/default";
	static final String THUMB_SIZE = "104c,72c,800";
	static final String IMG_SIZE = "1600";//"d";
	
	private static final int WIDTH = 1600;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	public ListPublicPhotos() {
		try {

			/**
			 * parse options
			 */
			Options options = new Options();
			options.addOption("h", false, "help");
			options.addOption("v", false, "be verbose");
			Option user = new Option("u", true, "user [REQUIRED]");
			user.setArgName("username");
			user.setRequired(true);
			options.addOption(user);
			Option pass = new Option("p", true, "password [REQUIRED]");
			pass.setArgName("password");
			pass.setRequired(true);
			options.addOption(pass);
			Option titl = new Option("t", true, "album title");
			titl.setArgName("title");
			options.addOption(titl);
			Option px = new Option("px", true, "resolution [px], default 1600");
			px.setArgName("px");
			options.addOption(px);			
			Option desc = new Option("d", true, "album description");
			desc.setArgName("description");
			options.addOption(desc);
			options.addOption("l", false, "list albums");
			CommandLine cmd = null;
			try {
				cmd = new PosixParser().parse(options, args);
			} catch (MissingOptionException e) {
				syntax(options);
				return;
			}
			if (cmd.hasOption("h")) {
				syntax(options);
				return;
			}
			String title = cmd.hasOption("t") && cmd.getOptionValue("t") != null ? cmd.getOptionValue("t") : sdf.format(new Date());
			String descr = cmd.hasOption("d") && cmd.getOptionValue("d") != null ? cmd.getOptionValue("d") : null;
			int width = WIDTH;
			if(cmd.hasOption("px") && cmd.getOptionValue("px") != null) {
				try {
					width = Integer.parseInt(cmd.getOptionValue("px"));
				} catch (NumberFormatException e) {
				}
			}
			
			/**
			 * auth
			 */
			log.info("LOGGING...");
			PicasawebService myService = new PicasawebService("exampleCo-exampleApp-1");
			myService.setUserCredentials(cmd.getOptionValue("u"), cmd.getOptionValue("p"));

			URL feedUrl = new URL(API_PREFIX + "default");

			/**
			 * -l switch = list albums
			 */
			if (cmd.hasOption("l")) {
				File f = File.createTempFile("/tmp", ".public");
				log.info(f.getAbsolutePath());
				FileWriter fw = new FileWriter(f);
				
				log.info("PUBLIC_ALBUMS/PHOTOS");
				UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);
				for (AlbumEntry myAlbum : myUserFeed.getAlbumEntries()) {
					
					if(myAlbum.getTitle().getPlainText().endsWith("\u00A0")) {
						log.info(myAlbum.getTitle().getPlainText());
						fw.write(myAlbum.getTitle().getPlainText().replaceFirst("\u00A0", "")+"\n");
					
					boolean logged = false;
					int max = 100;
					int start = 0;
					feedUrl = new 
URL(API_FEED_URL+"/albumid/"+myAlbum.getGphotoId()+"?kind=photo"+"&thumbsize="+THUMB_SIZE+"&imgmax="+IMG_SIZE+
							(logged ?
									
"&fields=id,title,entry(title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:thumbnail,media:group/media:content,media:group/media:keywords),openSearch:totalResults,openSearch:startIndex,openSearch:itemsPerPage"	
:
									
"&fields=title,openSearch:totalResults,openSearch:startIndex,openSearch:itemsPerPage,entry[media:group/media:keywords='public'%20or%20media:group/media:keywords='public,%20picnik'%20or%20media:group/media:keywords='picnik,%20public'](title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:thumbnail,media:group/media:content,media:group/media:keywords)")+
							(logged ? 
									"&max-results="+max+"&start-index="+start : 
									"")
							//+(session("user") != null ? "" : "&tag=public") /* to rozsortowuje kolejnosc fotek! */
							//+,exif:tags)"*/
							);
					
					
					//log.info(feedUrl.toString());
					Query photosQuery = new Query(feedUrl);

					// AlbumFeed feed = myService.getFeed(feedUrl, AlbumFeed.class);		
					AlbumFeed feed = myService.query(photosQuery, AlbumFeed.class);
					// if(feed.getTitle().getPlainText().endsWith("\u00A0")) {
					
						for(GphotoEntry<PhotoEntry> e: feed.getEntries()) {
							log.info(" - "+e.getTitle().getPlainText());
							fw.write("^"+e.getTitle().getPlainText()+"\n");
						}
						fw.flush();
					}
					
				}
				fw.close();
			}
			
			log.info("DONE.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * syntax help
	 * @param options
	 */
	private void syntax(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar picasa-uploader.jar [options] <dir1|file1> <dir2|file2>...", options);
	}

	/**
	 * scan paths and return list of all files
	 * @param paths
	 * @param files
	 * @return
	 */
	private List<String> list(String[] paths, List<String> files) {
		if (files == null) {
			files = new ArrayList<String>();
		}

		for (String path : paths) {
			if (new File(path).isDirectory()) {
				File d = new File(path);
				List<String> f = new ArrayList<String>();
				for (File f1 : d.listFiles()) {
					f.add(f1.getAbsolutePath());
				}
				list(f.toArray(new String[d.listFiles().length]), files);
			}
			if (new File(path).isFile()) {
				files.add(new File(path).getAbsolutePath());
			}
		}

		return files;
	}

	/**
	 * main
	 * @param args
	 */
	public static void main(String[] args) {
		ListPublicPhotos.args = args;
		new ListPublicPhotos();
	}
}

