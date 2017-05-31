package com.veridu.morpheus.tasks.models;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.google.gson.JsonArray;
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
import com.veridu.morpheus.utils.Parameters;
import com.veridu.morpheus.utils.PhotoParameters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cassio on 5/26/17.
 */
@Component("photo")
public class PhotoTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    private BeanConfigurationManager configManager;

    private static final Logger log = Logger.getLogger(PhotoTask.class);

    private static final IFact fact = new Fact("photoRecognitionResults", "morpheus");

    /**
     * Constructor
     *
     * @param utils injected utils bean
     * @param dao injected idOS SQL data source
     * @param configManager injected configuration manager
     */
    @Autowired
    public PhotoTask(IUtils utils, IDataSource dao, BeanConfigurationManager configManager) {
        this.utils = utils;
        this.dao = dao;
        this.configManager = configManager;
    }

    private String removeSlashes(String url) {
        if (url.startsWith("/"))
            return url.substring(1);
        return url;
    }

    private boolean rekogImageMatch(AmazonRekognition rekognitionClient, byte[] img1, byte[] img2) {
        Image im1 = new Image().withBytes(ByteBuffer.wrap(img1));
        Image im2 = new Image().withBytes(ByteBuffer.wrap(img2));

        CompareFacesRequest request = new CompareFacesRequest().withSourceImage(im1).withTargetImage(im2)
                .withSimilarityThreshold(new Float(0));

        CompareFacesResult compareFacesResult = rekognitionClient.compareFaces(request);

        List<CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
        for (CompareFacesMatch match : faceDetails) {
            ComparedFace face = match.getFace();
            if (match.getSimilarity() >= Constants.AWS_MIN_PHOTO_SIMILARITY) {
                return true;
            }
        }

        return false;
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

    /**
     * Run an Photo recognition task
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

        PhotoParameters photoParams = (PhotoParameters) params;

        if (!(params instanceof PhotoParameters)) {
            log.error("Photo recognition task did not receive parameters for handling user " + user.getId());
            return;
        }

        String base64docImg = ((PhotoParameters) params).docImage;
        String base64selfieImg = ((PhotoParameters) params).selfieImage;

        AWSCredentials credentials;

        try {
            credentials = new BasicAWSCredentials(configManager.getAWSaccessKey(), configManager.getAWSsecret());
        } catch (Exception e) {
            throw new AmazonClientException("Could not load AWS credentials.");
        }

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        byte[] docDecoded = Base64.getDecoder().decode(base64docImg);
        byte[] selfieDecoded = Base64.getDecoder().decode(base64selfieImg);

        boolean docPicMatchedSelfie = rekogImageMatch(rekognitionClient, docDecoded, selfieDecoded);

        ArrayList<String> picsProviders = new ArrayList<>();
        boolean[] matches = null;

        if (docPicMatchedSelfie) {
            // obtain social network profile pics
            HashMap<IFact, String> profilePics = this.dao.obtainSpecificFactForUser(factory, user, "profilePicture");
            matches = new boolean[profilePics.size()];
            int i = 0;

            for (IFact pictureFact : profilePics.keySet()) {
                picsProviders.add(pictureFact.getProvider());
                try {
                    URL url = new URL(profilePics.get(pictureFact));
                    BufferedImage img = ImageIO.read(url);
                    boolean result = rekogImageMatch(rekognitionClient, selfieDecoded,
                            convertBufferedImageToBytes(img));
                    matches[i++] = result;
                } catch (IOException e) {
                    log.error(String.format("PhotoTask could not download image at %s for user %s",
                            profilePics.get(pictureFact), user.getId()));
                }
            }
        }

        // write results
        JsonObject featureJson = new JsonObject();

        JsonArray socialPicsProvidersArr = new JsonArray();
        JsonArray socialPicsMatchesArr = new JsonArray();

        for (int i = 0; i < picsProviders.size(); i++) {
            socialPicsProvidersArr.add(picsProviders.get(i));
            socialPicsMatchesArr.add(matches[i]);
        }

        featureJson.addProperty("docPicMatchSelfie", docPicMatchedSelfie);
        featureJson.add("socialPicProviders", socialPicsProvidersArr);
        featureJson.add("socialPicMatchesWithSelfie", socialPicsMatchesArr);

        this.dao.insertFactForUser(factory, user, fact, featureJson.toString());

    }

}
