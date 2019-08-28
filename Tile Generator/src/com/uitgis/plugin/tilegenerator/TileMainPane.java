package com.uitgis.plugin.tilegenerator;

import java.io.IOException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.uitgis.plugin.tilegenerator.model.WizardData;
import com.uitgis.sdk.controls.MapControl;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class TileMainPane extends BorderPane {

	public TileMainPane(MapControl mapControl) {

		final Injector injector = Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				WizardData model = new WizardData();
				bind(WizardData.class).toInstance(model);

			}

		});

//		ClassLoader cl = FXMLLoader.getDefaultClassLoader(); //same as ClassLoader.getSystemClassLoader();
		ClassLoader loader = getClass().getClassLoader();
//		URL[] urls = ((URLClassLoader) loader).getURLs();
//		System.out.println("LOADER ->" + loader + urls.toString());
//		for (URL u : urls) {
//			System.out.println("------> TILE GEN LOADER--------!" + u.getFile());
//		}

		// Set classloader from Main app to Plugin to make sure plugin can find classes
		// from it self instead of Maple application, with base classes will be automatically delegated
		// to SystemClassLoader
		FXMLLoader.setDefaultClassLoader(loader);

		Parent p;
		try {
			p = FXMLLoader.load(TileMainPane.class.getResource("/fxml/Wizard.fxml"), null, new JavaFXBuilderFactory(),
					(clazz) -> injector.getInstance(clazz));
			setCenter(p);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
