package com.veridu.morpheus.tasks.models;

import com.google.cloud.vision.spi.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.OCRParameters;
import com.veridu.morpheus.utils.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by cassio on 5/23/17.
 */
@Component("ocr")
public class OCRTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    private static final Logger log = Logger.getLogger(OCRTask.class);

    private static final IFact fact = new Fact("ocrRecognitionResults", "morpheus");

    /**
     * Constructor
     *
     * @param utils injected utils bean
     * @param dao injected idOS SQL data source
     */
    @Autowired
    public OCRTask(IUtils utils, IDataSource dao) {
        this.utils = utils;
        this.dao = dao;
    }

    private double similarity(String text, String name, int pos, int nameLength) {
        if (pos < 0)
            return 0;

        String word = text.substring(pos, pos + nameLength);
        return LocalUtils.normalizedLevenshteinSimilarity(word, name);
    }

    private double checkName(String text, String name, int sliceSize) {
        int nameLength = name.length();

        if (nameLength <= sliceSize) { // the name is really short
            int pos = text.indexOf(name);  // will have to be an exact match
            return pos < 0 ? 0 : 1;
        }

        // perform a fuzzy comparison

        // forward search (use the beginning of the name)
        String nameStart = name.substring(0, sliceSize);
        int pos = text.indexOf(nameStart);
        double fwdSimilarity = similarity(text, name, pos, nameLength);

        // backwards search (use the end of the name)
        String nameEnd = name.substring(sliceSize, nameLength);
        pos = text.indexOf(nameEnd);
        double backSimilarity = similarity(text, name, pos - sliceSize, nameLength);

        return Math.max(fwdSimilarity, backSimilarity);
    }

    /**
     * Run an OCR prediction task
     * @param params request parameters
     */
    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis();

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;
        IUser user = new User(userId);

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        OCRParameters ocrParams = (OCRParameters) params;

        if (!(params instanceof OCRParameters)) {
            log.error("OCR Task did not receive parameters for handling user " + user.getId());
            return;
        }

        String[] names = ((OCRParameters) params).names;
        String base64Img = ((OCRParameters) params).image;
        byte[] imgDecoded = Base64.getDecoder().decode(base64Img);

        while (imgDecoded.length > Constants.MAX_GOOGLE_IMG_SIZE) {
            // we have to rescale the image, as google cant handle large images
            try {
                BufferedImage imgIO = ImageIO.read(new ByteArrayInputStream(imgDecoded));
                int height = imgIO.getHeight();
                int width = imgIO.getWidth();
                Scalr.resize(imgIO, Scalr.Method.AUTOMATIC, width / 2, height / 2);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(imgIO, "jpg", baos);
                imgDecoded = baos.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        try {
            ImageAnnotatorClient vision = ImageAnnotatorClient.create();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            ByteString bs = ByteString.copyFrom(imgDecoded);
            Image img = Image.newBuilder().setContent(bs).build();

            Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

            requests.add(request);

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    log.error("Problem during Google Vision API call: " + res.getError());
                    return;
                }
            }

            if (responses.size() < 1 || !responses.get(0).hasFullTextAnnotation()) {
                log.error("Could not get any text annotation from Google Vision.");
                return;
            }

            EntityAnnotation entityAnnotation = responses.get(0).getTextAnnotations(0);
            String text = entityAnnotation.getDescription();

            String normalizedText = text.toLowerCase().replace("\n", "").replace(" ", "");

            double[] similarities = new double[names.length];

            for (int i = 0; i < similarities.length; i++)
                similarities[i] = checkName(normalizedText, StringUtils.stripAccents(names[i].toLowerCase()), 3);

            JsonObject featureJson = new JsonObject();

            JsonArray namesArr = new JsonArray();
            JsonArray similaritiesArr = new JsonArray();

            for (int i = 0; i < names.length; i++) {
                namesArr.add(names[i]);
                similaritiesArr.add(similarities[i]);
            }

            featureJson.add("names", namesArr);
            featureJson.add("similarities", similaritiesArr);

            this.dao.insertFactForUser(factory, user, fact, featureJson.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
