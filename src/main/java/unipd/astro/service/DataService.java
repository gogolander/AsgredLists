/**
 * Copyright (C) 2015 Vincenzo Abate <gogolander@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package unipd.astro.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import unipd.astro.AsgredLists;
import unipd.astro.SplashScreen;
import unipd.astro.repository.*;

public class DataService {
	private static Logger log = Logger.getLogger(DataService.class.getName());
	private static DataService instance = null;

	private Properties appProperties = null;
	private ApplicationContext context;
	private ImageRepository imageRepository;
	private FlatRepository flatRepository;
	private LampRepository lampRepository;
	private ScienceRepository scienceRepository;
	private StandardRepository standardRepository;
	private ObservationRepository observationRepository;

	private StandardAtlas standardAtlas;

	public static synchronized DataService getInstance() {
		if (instance == null) {
			SplashScreen.getInstance().setProgress("Creating database...", 1);
			log.info("DataService doesn't exist.");
			log.info("Creating DataService...");
			instance = new DataService();
			log.info("DataService created.");
			log.info("Creating the database, parsing and loading beans...");
			instance.setApplicationContext(new ClassPathXmlApplicationContext("beans.xml"));
			log.info("Beans loaded.");
			log.info("Database created.");
		}
		return instance;
	}

	public static void close() {
		if (instance != null) {
			log.info("Closing context and unloading beans...");
			instance.closeContext();
			log.info("Beans unloaded.");
			instance = null;
		}
	}

	public FlatRepository getFlatRepository() {
		return flatRepository;
	}

	public LampRepository getLampRepository() {
		return lampRepository;
	}

	public ScienceRepository getScienceRepository() {
		return scienceRepository;
	}

	public StandardRepository getStandardRepository() {
		return standardRepository;
	}

	public ObservationRepository getObservationRepository() {
		return observationRepository;
	}

	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
		log.info("Loading ImageRepository...");
		SplashScreen.getInstance().setProgress("Creating ImageRepository...", 2);
		imageRepository = (ImageRepository) context.getBean("ImageRepository");
		log.info("Done.");
		log.info("Loading FlatRepository...");
		SplashScreen.getInstance().setProgress("Creating FlatRepository...", 12);
		flatRepository = (FlatRepository) context.getBean("FlatRepository");
		log.info("Done.");
		log.info("Loading LampRepository...");
		SplashScreen.getInstance().setProgress("Creating LampRepository...", 22);
		lampRepository = (LampRepository) context.getBean("LampRepository");
		log.info("Done.");
		log.info("Loading ScienceRepository...");
		SplashScreen.getInstance().setProgress("Creating ScienceRepository...", 32);
		scienceRepository = (ScienceRepository) context.getBean("ScienceRepository");
		log.info("Done.");
		log.info("Loading StandardRepository...");
		SplashScreen.getInstance().setProgress("Creating StandardRepository...", 42);
		standardRepository = (StandardRepository) context.getBean("StandardRepository");
		log.info("Done.");
		log.info("Loading ObservationRepository...");
		SplashScreen.getInstance().setProgress("Creating ObservationRepository...", 52);
		observationRepository = (ObservationRepository) context.getBean("ObservationRepository");
		log.info("Done.");
		log.info("Loading StandardAtlas...");
		SplashScreen.getInstance().setProgress("Creating StandardAtlas...", 62);
		standardAtlas = (StandardAtlas) context.getBean("StandardAtlas");
		SplashScreen.getInstance().setProgress("Database created.", 72);
		log.info("Done.");
	}

	public ImageRepository getImageRepository() {
		return this.imageRepository;
	}

	public StandardAtlas getStandardAtlas() {
		return this.standardAtlas;
	}

	private void closeContext() {
		((org.springframework.context.ConfigurableApplicationContext) context).close();
	}

	public String getProperty(String key) {
		if (appProperties == null)
			initProperties();
		return appProperties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		try {
			if (appProperties == null)
				initProperties();
			appProperties.setProperty(key, value);
			log.info("Saving...");
			appProperties.store(
					new FileOutputStream(Paths.get(System.getProperty("user.home"), "asgredLists.properties").toFile()),
					"Default value created by " + AsgredLists.class.getName());
			log.info("Done.");
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
	}

	private void initProperties() {
		try {
			String[] params = new String[] { "iraf.home", "iraf.wlcal.rms_threshold", "iraf.bg.col1", "iraf.bg.col2",
					"iraf.imcopy.start", "iraf.imcopy.end", "iraf.bg.options", "iraf.apall.options",
					"iraf.prered2.options", "iraf.wlcal.options", "iraf.scombine.options", "iraf.imcopy.options",
					"iraf.fcal.options" };
			String[] defaultValues = new String[] { System.getProperty("user.home"), "10", "1000", "1010", "50", "2040",
					"axis=2, order=7, mode=\"ql\"", "t_order = 3., t_niter = 5, b_order=7",
					"trimsec=\"[1:2040,40:490]\", mode=\"ql\"", "mode=\"ql\"", "reject=\"minmax\"", "", "" };

			log.info("Does the file ${user.home}/asgredLists.properties exist?");
			try {
				appProperties = new Properties();
				appProperties.load(new FileInputStream(
						Paths.get(System.getProperty("user.home"), "asgredLists.properties").toFile()));
				try {
					log.info("Yes. Is it valid?");
					boolean conflicts = false;
					for (int i = 0; i < params.length && !conflicts; i++)
						conflicts = (appProperties.getProperty(params[i]) == null
								|| appProperties.getProperty(params[i]).isEmpty());

					if (conflicts) {
						log.info("No. Setting to default value all entries...");
						for (int i = 0; i < params.length; i++)
							appProperties.setProperty(params[i], defaultValues[i]);
						log.info("Saving...");
						appProperties.store(
								new FileOutputStream(
										Paths.get(System.getProperty("user.home"), "asgredLists.properties").toFile()),
								"Default value created by " + AsgredLists.class.getName());
						log.info("Done.");
					} else
						log.info("Yes. Nothing to do.");
				} catch (Exception ex) {
					log.fatal(ex.getMessage(), ex);
				}
			} catch (FileNotFoundException ex) {
				log.info("No. Creating it...");
				Properties properties = new Properties();
				for (int i = 0; i < params.length; i++)
					properties.setProperty(params[i], defaultValues[i]);
				log.info("Saving...");
				properties.store(new FileOutputStream(
						Paths.get(System.getProperty("user.home"), "asgredLists.properties").toFile()), "");
				log.info("Done.");
			}
			log.info("asgredLists.properties set.");
		} catch (Exception ex) {
			log.error(ex);
		}
	}

	public void restoreProperties() {
		try {
			String[] params = new String[] { "iraf.home", "iraf.wlcal.rms_threshold", "iraf.bg.col1", "iraf.bg.col2",
					"iraf.imcopy.start", "iraf.imcopy.end", "iraf.bg.options", "iraf.apall.options",
					"iraf.prered2.options", "iraf.wlcal.options", "iraf.scombine.options", "iraf.imcopy.options",
					"iraf.fcal.options" };
			String[] defaultValues = new String[] { System.getProperty("user.home"), "10", "1000", "1010", "50", "2040",
					"axis=2, order=7, mode=\"ql\"", "t_order = 3., t_niter = 5, b_order=7",
					"trimsec=\"[1:2040,40:490]\", mode=\"ql\"", "mode=\"ql\"", "reject=\"minmax\"", "", "" };

			log.info("Restoring default values...");
			Properties properties = new Properties();
			for (int i = 0; i < params.length; i++)
				properties.setProperty(params[i], defaultValues[i]);
			log.info("Saving...");
			properties.store(
					new FileOutputStream(Paths.get(System.getProperty("user.home"), "asgredLists.properties").toFile()),
					"");
			log.info("Done.");
		} catch (Exception ex) {
			log.error(ex);
		}

	}
}
