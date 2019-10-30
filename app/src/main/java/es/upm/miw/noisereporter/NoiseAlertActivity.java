package es.upm.miw.noisereporter;


import es.upm.miw.firebaselogin.R;
import es.upm.miw.noisereporter.fcube.commands.FCColor;
import es.upm.miw.noisereporter.fcube.commands.FCOff;
import es.upm.miw.noisereporter.fcube.commands.FCOn;
import es.upm.miw.noisereporter.fcube.config.FeedbackCubeConfig;
import es.upm.miw.noisereporter.fcube.config.FeedbackCubeManager;
import es.upm.miw.noisereporter.feeback.FeedbackColor;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;


public class NoiseAlertActivity extends IntentService {
    static String sIp = "192.168.0.100";

    public NoiseAlertActivity() {
        super("NoiseAlertActivity");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        sendColor(new FeedbackColor(0, 0, 255));
    }

    public void sendColor(FeedbackColor color) {
        // Launch color in the cube
        FCColor fcc = new FCColor(sIp, "" + color.getR(), ""
                + color.getG(), "" + color.getB());
        new FeedbackCubeManager().execute(fcc);
    }

    public void encenderCubo() {
        FCOn f = new FCOn(sIp);
        new FeedbackCubeManager().execute(f);
    }

    public void apagarCubo() {
        FCOff f = new FCOff(sIp);
        new FeedbackCubeManager().execute(f);
    }


}
