/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.tasks.models;

import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.BeanConfigurationManager;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.Parameters;
import com.veridu.morpheus.utils.PhotoParameters;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cassio on 6/7/17.
 */
@Component("photo-qa")
public class PhotoQualityCheckTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    private BeanConfigurationManager configManager;

    private static final Logger log = Logger.getLogger(PhotoTask.class);

    private static final IFact fact = new Fact("photoQualityCheck", "morpheus");

    /**
     * Constructor
     *
     * @param utils injected utils bean
     * @param dao injected idOS SQL data source
     * @param configManager injected configuration manager
     */
    @Autowired
    public PhotoQualityCheckTask(IUtils utils, IDataSource dao, BeanConfigurationManager configManager) {
        this.utils = utils;
        this.dao = dao;
        this.configManager = configManager;
    }

    class QAresult {
        public boolean brightnessOK;
        public boolean landscapeOK;
        public boolean fileFormatOK;
        public boolean resolutionOK;

        public QAresult(boolean brightnessOK, boolean landscapeOK, boolean fileFormatOK, boolean resolutionOK) {
            this.brightnessOK = brightnessOK;
            this.landscapeOK = landscapeOK;
            this.fileFormatOK = fileFormatOK;
            this.resolutionOK = resolutionOK;
        }
    }

    private static boolean evaluateBrightness(double[] pixels) {
        Percentile percentile = new Percentile();
        return percentile.evaluate(pixels, Constants.PHOTO_BRIGHTNESS_PERCENTILE) > Constants.PHOTO_MIN_BRIGHTNESS ?
                true :
                false;
    }

    private static boolean evaluateLandscape(FImage image) {
        return image.getWidth() > image.getHeight() ? true : false;
    }

    private static boolean evaluateResolution(FImage image) {
        return image.getWidth() > Constants.MIN_IMAGE_RESOLUTION && image.getHeight() > Constants.MIN_IMAGE_RESOLUTION ?
                true :
                false;
    }

    private static boolean evaluateFaceDetection(FImage image, int expectedNumFaces) {
        FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(30);
        List<DetectedFace> faces = fd.detectFaces(image);
        return faces.size() == expectedNumFaces ? true : false;
    }

    private byte[] convertBufferedImageToBytes(BufferedImage bimg) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bimg, "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private boolean evaluateFileFormat(byte[] image) {
        try {
            ImageInputStream is = ImageIO.createImageInputStream(new ByteArrayInputStream(image));
            Iterator<ImageReader> readers = ImageIO.getImageReaders(is);
            while (readers.hasNext()) {
                ImageReader ir = readers.next();
                String fmt = ir.getFormatName();
                if (fmt.equals("JPEG") || fmt.equals("PNG"))
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private MBFImage obtainMBFImage(byte[] img) throws IOException {
        return ImageUtilities.readMBF(new ByteArrayInputStream(img));
    }

    private QAresult qaCheck(FImage img, double[] pixels, byte[] bimg) {
        boolean bright = evaluateBrightness(pixels);
        boolean landscape = evaluateLandscape(img);
        boolean fileFormat = evaluateFileFormat(bimg);
        boolean resolution = evaluateResolution(img);
        return new QAresult(bright, landscape, fileFormat, resolution);
    }

    private void appendResults(JsonObject featureJson, String prop, QAresult qaResults) {
        JsonObject obj = new JsonObject();
        obj.addProperty("brightnessOK", qaResults.brightnessOK);
        obj.addProperty("landscapeOK", qaResults.landscapeOK);
        obj.addProperty("fileFormatOK", qaResults.fileFormatOK);
        obj.addProperty("resolutionOK", qaResults.resolutionOK);
        featureJson.add(prop, obj);
    }

    /**
     * Run an Photo recognition task
     * @param params request parameters
     */
    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;
        IUser user = new User(userId);

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        if (!(params instanceof PhotoParameters)) {
            log.error("Photo quality check task did not receive parameters for handling user " + user.getId());
            return;
        }

        String base64docImg = ((PhotoParameters) params).docImage;
        String base64selfieImg = ((PhotoParameters) params).selfieImage;

        try {
            byte[] docDecoded = LocalUtils.resize(Base64.getDecoder().decode(base64docImg), 500000);
            byte[] selfieDecoded = Base64.getDecoder().decode(base64selfieImg);

            MBFImage selfieImage = obtainMBFImage(selfieDecoded);
            MBFImage docImage = obtainMBFImage(docDecoded);

            FImage selfieGray = Transforms.calculateIntensity(selfieImage);
            FImage docGray = Transforms.calculateIntensity(docImage);

            double[] selfiePixels = selfieGray.getDoublePixelVector();
            double[] docPixels = docGray.getDoublePixelVector();

            QAresult selfieResults = qaCheck(selfieGray, selfiePixels, selfieDecoded);
            QAresult docResults = qaCheck(docGray, docPixels, docDecoded);

            // write results
            JsonObject featureJson = new JsonObject();
            appendResults(featureJson, "selfie", selfieResults);
            appendResults(featureJson, "doc", docResults);

            this.dao.insertFactForUser(factory, user, fact, featureJson.toString());

            if (verbose) {
                log.info("Photo Quality Check for user " + user.getId() + " => " + featureJson.toString());
            }
        } catch (IOException e) {
            log.error("Photo Quality Check failed for user " + user.getId());
            e.printStackTrace();
        }
    }

}
