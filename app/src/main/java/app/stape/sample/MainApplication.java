package app.stape.sample;

import android.app.Application;

import app.stape.Stape;
import app.stape.StapeConfig;
import app.stape.StapeConfigBuilder;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StapeConfig config = StapeConfigBuilder.make().isDebug(true).build(this);
        Stape.init(this, config);
    }
}
