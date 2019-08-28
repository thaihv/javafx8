package com.uitgis.plugin.tilegenerator.app;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import com.uitgis.plugin.tilegenerator.app.TileGenApplication;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TileGenApplicationTest extends ApplicationTest {
	
    @Override 
    public void start(Stage stage) {
        Parent sceneRoot = new TileGenApplication.ClickPane();
        Scene scene = new Scene(sceneRoot, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    @Test 
    public void should_contain_button() {
        // expect:
        verifyThat(".button", hasText("click me!"));
    }

    @Test 
    public void should_click_on_button() {
        // when:
        clickOn(".button");

        // then:
        verifyThat(".button", hasText("clicked!"));
    }
}