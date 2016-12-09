package com.brain_socket.thagheralrafedain.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.brain_socket.thagheralrafedain.R;
import com.brain_socket.thagheralrafedain.ThagherApp;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.ShareDialog;

public class FacebookProvider {
    private static FacebookProvider facebookProvider = null;

    public enum STORY_TYPE {
        NONE, WIN, LOSSE, BOOK
    };

    private static final String TAG = "FbProvider";
    // story actions urls
    private final static String READS_URL = "me/books.reads";
    private final static String WON_TYPE = "molham_api_test:win";
    private final static String WON_URL = "me/" + WON_TYPE;

    // story objects URLs
    private final static String BOOK_URL = "me/objects/books.book";
    private final static String GAME_TYPE = "game";
    private final static String GAME_URL = "me/objects/" + GAME_TYPE;

    // private Session.StatusCallback statusCallback = new
    // SessionStatusCallback();

    private FacebookCallback<LoginResult> loginCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            handlePendingAction();
            final AccessToken accessToken = loginResult.getAccessToken();
            //Profile profile = Profile.getCurrentProfile();
            if (accessToken != null & !accessToken.isExpired()){
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,GraphResponse response) {
                                try{
                                    // Application code
                                    Log.v("LoginActivity", response.toString());
                                    HashMap<String, Object> map = new HashMap<String, Object>();
                                    JSONObject jsonResp = response.getJSONObject();
                                    try
                                    {
                                        map.put("name", jsonResp.get("name"));
                                    }catch(Exception ex){
                                        map.put("name", "unknown");
                                    }
                                    try
                                    {
                                        map.put("email", jsonResp.get("email"));
                                    }catch(Exception ex){
                                        map.put("email", "");
                                    }
                                    try
                                    {
                                        map.put("gender", jsonResp.get("gender"));
                                    }
                                    catch(Exception ex){
                                        ex.printStackTrace();
                                    }
                                    try
                                    {
                                        map.put("birthday", jsonResp.get("birthday"));
                                    }
                                    catch(Exception ex){
                                        ex.printStackTrace();
                                    }
                                    broadcastSessionOpened(accessToken.getToken(), accessToken.getUserId(), map);
                                }catch (Exception e) {
                                    broadcastFacebookException(null);
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }
        }

        @Override
        public void onCancel() {
            Log.d("FB", "login Canseled");
        }

        @Override
        public void onError(FacebookException exception) {
            broadcastFacebookException(exception);
        }
    };
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            if (shareListener != null)
                shareListener.onShareCancelled();
        }

        @Override
        public void onError(FacebookException error) {
            if (shareListener != null)
                shareListener.onShareError(ThagherApp.getAppContext().getString(R.string.err_sharing_failed));
            broadcastFacebookException(error);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            if (shareListener != null) {
                shareListener.onShareResult(true);
            }
        }
    };
    FacebookCallback<AppInviteDialog.Result> appInviteCallback = new FacebookCallback<AppInviteDialog.Result>() {
        @Override
        public void onSuccess(AppInviteDialog.Result result) {
            Log.d("FB", "inv Success");
        }

        @Override
        public void onCancel() {
            Log.d("FB", "inv fail");
        }

        @Override
        public void onError(FacebookException error) {
            broadcastFacebookException(error);
        }
    };

    GraphRequest.Callback imageUploadCallback = new GraphRequest.Callback() {
        @Override
        public void onCompleted(GraphResponse response) {
            // Log any response error
            FacebookRequestError error = response.getError();
            if (error != null) {
                // dismissProgressDialog();
                // Log.i(TAG, error.getErrorMessage());
            }
        }
    };
    GraphRequest.Callback createObjectCallback = new GraphRequest.Callback() {

        @Override
        public void onCompleted(GraphResponse response) {
            // Log any response error
            FacebookRequestError error = response.getError();
            if (error != null) {
                // dismissProgressDialog();
                Log.i(TAG, error.getErrorMessage());
            }
        }
    };

    GraphRequest.Callback actionCallback = new GraphRequest.Callback() {

        @Override
        public void onCompleted(GraphResponse response) {
            FacebookRequestError error = response.getError();
            if (error != null) {
                if (shareCallback != null)
                    shareCallback.onError(null);
                // Toast.makeText(activity.getApplicationContext(),error.getErrorMessage(),Toast.LENGTH_LONG).show();
            } else {
                String actionId = null;
                try {
                    JSONObject graphResponse = response.getJSONObject();
                    actionId = graphResponse.getString("id");
                } catch (Exception e) {
                    Log.i(TAG, "actionCallback error " + e.getMessage());
                }
                // Toast.makeText(activity.getApplicationContext(),actionId,Toast.LENGTH_LONG).show();
                if (shareCallback != null)
                    shareCallback.onSuccess(null);
            }
        }
    };

    // ArrayList<FacebookProviderListener> arrayListeners = new
    // ArrayList<FacebookProviderListener>();
    FacebookProviderListener listener = null;
    // sharing
    FacebookSharingListener shareListener = null;
    private AppSocialPendingAction pendingShareAction = null;

    private enum PendingAction {
        NONE, POST_PHOTO, POST_STATUS_UPDATE, SEND_INVITATION, GET_FRIENDS, POST_STORY
    }

    private PendingAction pendingAction = PendingAction.NONE;
    // private Bitmap bmPhoto = null;
    private File filePhoto = null;
    private String captionPhoto = null;
    // private static final int REQUEST_CODE_PERMISSION_PUBLISH = 30;

    private static final List<String> PERMISSIONS = new ArrayList<String>() {
        {
            add("user_friends");
            add("public_profile");
            add("email");
        }
    };
    private static final String PUBLISH_PERMISSION = "publish_actions";
    private CallbackManager callbackManager;

    private FacebookProvider() {
        try {
            // add Access Token request from the Facebook SDK Settings
            // Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.CACHE);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.DEVELOPER_ERRORS);
            FacebookSdk.sdkInitialize(ThagherApp.getAppContext());
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager,loginCallback);
        } catch (Exception e) {
        }
    }

	/*
	 * ProfileTracker profileTracker = new ProfileTracker() {
	 *
	 * @Override protected void onCurrentProfileChanged(Profile oldProfile,
	 * Profile currentProfile) { handlePendingAction(); } };
	 */

    /**
     * Returns the singleton of the FacebookProviderListener class
     *
     * @return
     */
    public static FacebookProvider getInstance() {
        if (facebookProvider == null) {
            facebookProvider = new FacebookProvider();
        }
        return facebookProvider;
    }

    /**
     * Requests the Facebook SDK to login the current user.
     *
     * @param activity
     */
    public void requestFacebookLogin(Activity activity) {
        try {
            AccessToken token = AccessToken.getCurrentAccessToken();
            LoginManager.getInstance().logInWithReadPermissions(activity, PERMISSIONS);
            // LoginManager.getInstance().logInWithPublishPermissions(activity,
            // null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestFacebookLogout() {
        try {
            LoginManager.getInstance().logOut();
        } catch (Exception e) {
        }
    }

/*	public void requestFacebookFriends(Activity activity) {
		try {
			AccessToken token = AccessToken.getCurrentAccessToken();
			if (token == null) {
				LoginManager.getInstance().logInWithReadPermissions(activity,
						PERMISSIONS);
				pendingAction = PendingAction.GET_FRIENDS;
			} else {
				sendGetFriendsRequest(token);
			}
		} catch (Exception e) {
		}
	}*/

/*	private void sendGetFriendsRequest(AccessToken token) {
		try {
			GraphRequest request = GraphRequest.newGraphPathRequest(token,
					"me/friends", null);

			Set<String> fields = new HashSet<String>();
			String[] requiredFields = new String[] { "id",
					"name" , "picture" , "installed" };
			fields.addAll(Arrays.asList(requiredFields));

			Bundle parameters = request.getParameters();
			parameters.putString("fields", TextUtils.join(",", fields));
			// set request params
			request.setParameters(parameters);
			// set request callback
			request.setCallback(new GraphRequest.Callback() {
				@Override
				public void onCompleted(GraphResponse response) {
					ArrayList<AppFacebookFriend> arrayFriends = null;
					ArrayList<String> arrayActiveIds = null;
					// ArrayList<AppFacebookFriend> arrayFriendsActive = null;
					try {
						// String raw = response.getRawResponse() ;
						JSONObject result = response.getJSONObject();
						arrayFriends = new ArrayList<AppFacebookFriend>();
						JSONArray jsonFriends = null;
						if (result.has("data"))
							jsonFriends = result.getJSONArray("data");


						 * GraphMultiResult multiResult =
						 * response.getGraphObjectAs(GraphMultiResult.class);
						 * GraphObjectList<GraphObject> data = multiResult
						 * .getData(); List<GraphUser> friends = data
						 * .castToListOf(GraphUser.class);


						// arrayFriendsActive = new
						// ArrayList<AppFacebookFriend>();

						arrayActiveIds = new ArrayList<String>();
						if (jsonFriends != null) {
							for (int i = 0; i < jsonFriends.length(); i++) {
								try {
									JSONObject friend = jsonFriends
											.getJSONObject(i);
									String id = friend.getString("id");
									String name = friend.getString("name");
									boolean installed = false;
									String picture = null;
									if (id != null) {
										picture = "http://graph.facebook.com/"
												+ id + "/picture?type=square";
									}
									if (friend.getString("installed") != null) {
										installed = friend
												.getBoolean("installed");
									}
									// create friend object
									AppFacebookFriend fbFriend = new AppFacebookFriend(
											id, name, picture, installed);
									arrayFriends.add(fbFriend);
									if (installed && id != null) {
										arrayActiveIds.add(id);
									}
								} catch (Exception e) {
								}
							}
						}
					} catch (Exception e) {
					}
					broadcastFacebookFriends(arrayFriends, arrayActiveIds);
				}
			});
			// execute request
			request.executeAsync();
		} catch (Exception e) {
		}
	}*/

    public void sendInvitations(Activity activiy, String msg) {


        AppInviteContent content = new AppInviteContent.Builder()
                .setApplinkUrl("https://fb.me/1638639549688434")
                .setPreviewImageUrl("http://70marra.com/Screenshots/10-DoTask.jpg")
                .build();
        if (AppInviteDialog.canShow()) {
            AppInviteDialog appInviteDialog = new AppInviteDialog(activiy);
            appInviteDialog.registerCallback(callbackManager, appInviteCallback);
            appInviteDialog.show(content);
            //appInviteDialog.show(activiy, content);
        } else {
            //showError(R.string.appinvite_error);
        }


/*		AppInviteContent gameRequestContent = new AppInviteContent.Builder()
				.setTitle(activiy.getString(R.string.app_name))
				.setMessage(RosaryApp.getAppContext().getString(R.string.fb_invite_dialog_title))
				.build();

		 * AppInviteContent content = new AppInviteContent.Builder()
		 * .setApplinkUrl(Configuration.URL_SHARE_APP)
		 * .setPreviewImageUrl(Configuration.URL_SHARING_ICON) .build();

		if (GameRequestDialog.canShow()) {
			GameRequestDialog appInviteDialog = new GameRequestDialog(activiy);
			appInviteDialog.registerCallback(callbackManager, appInviteCallback);
			appInviteDialog.show(activiy, gameRequestContent);
		} else {

		}*/
    }

    public void sendInvitation(final Activity activity, final String userId,
                               final String dialogTitle, final String dialogMessage,
                               final String sucessMessage) {
        try {
            AccessToken token = AccessToken.getCurrentAccessToken();
            if (token == null) {
                LoginManager.getInstance().logInWithReadPermissions(activity,PERMISSIONS);
                pendingAction = PendingAction.SEND_INVITATION;
                pendingShareAction = new AppSocialPendingAction(activity, "",
                        "", "", null, STORY_TYPE.NONE);
            } else {
                sendInvitationRequest(activity, userId, dialogTitle,
                        dialogMessage, sucessMessage);
            }
        } catch (Exception e) {
        }
    }

    private void sendInvitationRequest(final Activity activity,
                                       final String userId, final String dialogTitle,
                                       final String dialogMessage, final String sucessMessage) {
        try {
			/*
			 * Bundle params = new Bundle(); if (dialogTitle != null &&
			 * !dialogTitle.isEmpty()) { params.putString("title", dialogTitle);
			 * } if (dialogMessage != null && !dialogMessage.isEmpty()) {
			 * params.putString("message", dialogMessage); } // comma seperated
			 * list of facebook IDs to preset the recipients. If // left out, it
			 * will show a Friend Picker. params.putString("to", userId); //
			 * your friend id WebDialog requestsDialog = (new
			 * WebDialog.RequestsDialogBuilder(activity, session, params))
			 * .setOnCompleteListener(new OnCompleteListener() {
			 *
			 * @Override public void onComplete(Bundle values, FacebookException
			 * error) { if (error != null) { } else { final String requestId =
			 * values.getString("request"); if (requestId != null) {
			 * Configuration.displayToast(sucessMessage, Toast.LENGTH_SHORT); }
			 * } } }).build(); requestsDialog.show();
			 */
        } catch (Exception e) {
        }
    }

    /**
     * Retrieves the access token from the active session if opened
     *
     * @return The accessToken if the session is opened, null otherwise
     */
	/*
	 * private String getSessionAccessToken() { String accessToken = null; try {
	 * Session session = Session.getActiveSession(); if(session != null) {
	 * accessToken = session.getAccessToken(); } } catch (Exception e) {} return
	 * accessToken; }
	 */

    public void registerShareListener(FacebookSharingListener shareListener) {
        this.shareListener = shareListener;
    }

    private boolean hasPublishPermission() {
        AccessToken at = AccessToken.getCurrentAccessToken();
        return at != null && at.getPermissions().contains(PUBLISH_PERMISSION);
    }

    private void handlePendingAction() {
        try {
            PendingAction previouslyPendingAction = pendingAction;
            // These actions may re-set pendingAction if they are still pending,
            // but
            // we assume they will succeed.
            pendingAction = PendingAction.NONE;

            switch (previouslyPendingAction) {
                case POST_PHOTO:
                    if (pendingShareAction != null) {
                        sharePhotoViaFacebook(pendingShareAction.getActivity());
                        pendingShareAction = null;
                    }
                    break;
                case POST_STATUS_UPDATE:
                    if (pendingShareAction != null) {
                        shareViaFacebook(pendingShareAction.getActivity(),
                                pendingShareAction.getDesrciption(),
                                pendingShareAction.getShareURL(),
                                pendingShareAction.getPictureURL());
                        pendingShareAction = null;
                    }
                    break;
			/*case GET_FRIENDS:
				AccessToken token = AccessToken.getCurrentAccessToken();
				if (token != null)
					sendGetFriendsRequest(token);
				break;*/
                case SEND_INVITATION:
                    // sendInvitations(activiy, msg);
                    break;
                case POST_STORY:
                    shareStory(pendingShareAction.getActivity(),
                            pendingShareAction.getStoryType(),
                            pendingShareAction.getDesrciption(),
                            pendingShareAction.getBitmap(),
                            pendingShareAction.getShareURL(),
                            Uri.parse(pendingShareAction.getPictureURL()));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

	/*
	 * private void postPhoto(File filePhoto, String captionPhoto){ try {
	 * if(hasPublishPermission()) { Request request =
	 * Request.newUploadPhotoRequest(Session.getActiveSession(), filePhoto, new
	 * Request.Callback() {
	 *
	 * @Override public void onCompleted(Response response) {
	 * FacebookRequestError error = response.getError(); if(error == null) {
	 * if(shareListener != null) { shareListener.onShareResult(true); } } else {
	 * if(shareListener != null) {
	 * shareListener.onShareError(Configuration.getString
	 * (R.string.sharing_failed)); } } // clear temp data pendingAction =
	 * PendingAction.NONE; FacebookProvider.this.filePhoto = null;
	 * FacebookProvider.this.captionPhoto = null; } }); if(captionPhoto != null)
	 * { Bundle params = request.getParameters(); params.putString("caption",
	 * captionPhoto); request.setParameters(params); } request.executeAsync(); }
	 * else { pendingAction = PendingAction.POST_PHOTO; if(shareListener !=
	 * null) {
	 * shareListener.onShareError(Configuration.getString(R.string.sharing_failed
	 * )); } } } catch (Exception e) { if(shareListener != null) {
	 * shareListener.
	 * onShareError(Configuration.getString(R.string.sharing_failed)); } } }
	 */

    public void sharePhotoViaFacebook(Activity activity) {
        try {
            Uri path = Uri.parse("http://khednym3ak.com/cover-fb.png");
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("title")
                    .setContentDescription("description")
                    .setContentUrl(path)
                    .setImageUrl(path)
                    .build();
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareDialog shareDiag = new ShareDialog(activity);
                shareDiag.registerCallback(callbackManager, shareCallback);
                //shareDiag.show(sharePhotoContent);
                ShareDialog.show(activity, linkContent);
            } else if (hasPublishPermission()) {
                ShareApi.share(linkContent, shareCallback);
            } else {
                pendingAction = PendingAction.POST_PHOTO;
                shareListener.onShareError(activity
                        .getString(R.string.err_fb_not_installed));
            }
        } catch (Exception e) {
        }
    }

    public void shareViaFacebook(final Activity activity,
                                 final String description, final String shareUrl,
                                 final String pictureURL) {
        try {

            // Profile profile = Profile.getCurrentProfile();
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Almwajaha")
                    .setContentDescription(description)
                    .setContentUrl(Uri.parse(shareUrl)).build();

            if (!hasPublishPermission()) {
                pendingShareAction = new AppSocialPendingAction(activity,
                        description, shareUrl, pictureURL, null,
                        STORY_TYPE.NONE);
                pendingAction = PendingAction.POST_STATUS_UPDATE;
                LoginManager.getInstance().logInWithPublishPermissions(
                        activity, Arrays.asList(PUBLISH_PERMISSION));
            } else if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareDialog shareDialog = new ShareDialog(activity);
                shareDialog.registerCallback(callbackManager, shareCallback);
                shareDialog.show(linkContent);
            } else {
                // shareListener.onShareError(activity.getString(R.string.message_fb_not_installed));
                ShareApi.share(linkContent, shareCallback);
            }
        } catch (Exception e) {
            if (shareListener != null) {
                shareListener.onShareError(activity.getString(R.string.err_operation_failed));
            }
        }
    }

    public void onActiviyResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void shareStory(Activity activity, STORY_TYPE type, String title,
                           Bitmap bitmap, String peerID, Uri imgURL) {
        try {
            shareStoryUsingDialog(activity, type, title, bitmap, peerID, imgURL);
			/*
			 * if(bitmap == null ){ bitmap=
			 * MediaStore.Images.Media.getBitmap(activity.getContentResolver(),
			 * imgURL); shareStoryUsingAPI(activity, type, bitmap, null); }
			 */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/*
	 * private void shareStoryUsingDialog(Activity activity, STORY_TYPE type
	 * ,String title, Bitmap bitmap, String peerID, Uri imgURL) {
	 *
	 * List<String> peers = new ArrayList<String>(); peers.add(peerID);
	 *
	 * ShareOpenGraphAction action ; ShareOpenGraphContent content = null;
	 * ShareOpenGraphObject obj ;
	 *
	 * final SharePhoto photo; if(bitmap == null && imgURL != null){ photo = new
	 * SharePhoto.Builder() .setImageUrl(imgURL).build() ; }else{ photo = new
	 * SharePhoto.Builder().setBitmap(bitmap).build(); }
	 *
	 * switch (type) { case WIN: String temp = "game" ; String objty = "og:type"
	 * ; obj = new ShareOpenGraphObject.Builder() .putString(objty,
	 * "books.book") .putString("books:isbn", "0-553-57340-3")
	 * .putString("og:title", "book title") //.putPhotoArrayList("og:image", new
	 * ArrayList<SharePhoto>() {{add(photo);}}) .build();
	 *
	 * action = new ShareOpenGraphAction.Builder() .setActionType("books:reads")
	 * .putObject("book", obj) .build();
	 *
	 * content = new ShareOpenGraphContent.Builder()
	 * .setPreviewPropertyName("book") .setAction(action) //.setPeopleIds(peers)
	 * .build();
	 *
	 * break; }
	 *
	 * if(content != null){ ShareDialog shareDiag = new ShareDialog(activity);
	 * shareDiag.registerCallback(callbackManager, shareCallback);
	 * if(ShareDialog.canShow(content.getClass())) shareDiag.show(content);
	 * //ShareDialog.show(activity, content); //ShareApi.share(content,
	 * shareCallback); } }
	 */

    private void shareStoryUsingDialog(Activity activity, STORY_TYPE type,
                                       String title, Bitmap bitmap, String peerID, Uri imgURL) {
        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:type", "testmolh:piano")
                .putString("og:title", "A Game of Thrones")
                .putString(
                        "og:description",
                        "In the frozen wastes to the north of Winterfell, sinister and supernatural forces are mustering.")
                // .putString("books:isbn", "0-553-57340-3")
                .build();

        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("testmolh:play").putObject("piano", object)
                .putBoolean("fb:explicitly_shared", true).build();

        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setAction(action).setRef("piano")
                .setPreviewPropertyName("piano").build();

        ShareDialog diag = new ShareDialog(activity);
        diag.registerCallback(callbackManager, shareCallback);
		/*
		 * if(diag.canShow(content, ShareDialog.Mode.NATIVE))
		 * ShareDialog.show(activity,content); else
		 */if (diag.canShow(content, ShareDialog.Mode.WEB))
            diag.show(content, ShareDialog.Mode.WEB);
        else
            diag.show(content, ShareDialog.Mode.FEED);

        // ShareDialog.show(activity, content);
    }

    public void shareStoryUsingAPI(Activity activity, STORY_TYPE type,
                                   Bitmap img, String imgURL) {
        // Settings.addLoggingBehavior(LoggingBehavior.REQUESTS);

        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token != null) {
            // List<String> permissions = token.getPermissions();
            if (!hasPublishPermission()) {
                pendingAction = PendingAction.POST_STORY;
                pendingShareAction = new AppSocialPendingAction(activity, null,
                        null, imgURL, null, STORY_TYPE.NONE);
                LoginManager.getInstance().logInWithPublishPermissions(
                        activity, Arrays.asList(PUBLISH_PERMISSION));
                return;
            }

            try {
                GraphRequestBatch requestBatch = new GraphRequestBatch();

                // If uploading an image, set up the first batch request to do
                // this.
                if (img != null) {
                    Bundle imageParams = new Bundle();
                    imageParams.putParcelable("file", img);
                    GraphRequest imageRequest = new GraphRequest(token,
                            "me/staging_resources", imageParams,
                            HttpMethod.POST, imageUploadCallback);
                    imageRequest.setBatchEntryName("imageUpload");
                    requestBatch.add(imageRequest);
                }

                JSONObject object = getStoryObject(STORY_TYPE.BOOK, false,
                        imgURL, "story title", "Story desc",
                        "http://google.com");
                String objUrl = BOOK_URL;
                String actionURL = null;
                String keyName = null;

                switch (type) {
                    case WIN:
                        object = getStoryObject(STORY_TYPE.WIN, false, imgURL,
                                "won story title", "won Story desc",
                                "http://google.com");
                        objUrl = GAME_URL;
                        actionURL = WON_URL;
                        keyName = "game";
                        break;
                    case BOOK:
                        object = getStoryObject(STORY_TYPE.BOOK, false, imgURL,
                                "book story title", "book Story desc",
                                "http://google.com");
                        objUrl = BOOK_URL;
                        actionURL = READS_URL;
                        keyName = "book";
                        break;

                }

                // Set up object request parameters
                Bundle objectParams = new Bundle();
                objectParams.putString("object", object.toString());

                GraphRequest objectRequest = new GraphRequest(
                        AccessToken.getCurrentAccessToken(), objUrl,
                        objectParams, HttpMethod.POST, createObjectCallback);
                objectRequest.setBatchEntryName("objectCreate");
                requestBatch.add(objectRequest);

                Bundle actionParams = new Bundle();
                actionParams.putString(keyName, "{result=objectCreate:$.id}");
                // actionParams.putString(keyName, "773072422788051");
                // Turn on the explicit share flag
                actionParams.putString("fb:explicitly_shared", "true");
                // jsonObj.put("image", "{result=imageUpload:$.uri}");
                actionParams.putString("image[0][url]",
                        "{result=objectCreate:$.image}");
                actionParams.putString("image[0][user_generated]", "true");

                GraphRequest actionRequest = new GraphRequest(
                        AccessToken.getCurrentAccessToken(), actionURL,
                        actionParams, HttpMethod.POST, actionCallback);
                requestBatch.add(actionRequest);

                requestBatch.executeAsync();

            } catch (Exception e) {
                Log.i(TAG, "JSON error " + e.getMessage());
            }
        }
    }

    private JSONObject getStoryObject(STORY_TYPE type, boolean UPLOAD_IMAGE,
                                      String imgUrl, String title, String desc, String url) {
        JSONObject jsonObj = new JSONObject();
        try {
            if (UPLOAD_IMAGE) {
                jsonObj.put("image", "{result=imageUpload:$.uri}");
            } else {
                jsonObj.put("image", imgUrl);
            }
            jsonObj.put("title", title);
            jsonObj.put("url", url);
            jsonObj.put("description", desc);
            switch (type) {
                case BOOK:
                    JSONObject data = new JSONObject();
                    data.put("isbn", "0-553-57340-3");
                    jsonObj.put("data", data);
                    break;
                case WIN:
                    jsonObj.put("type", "game");
                    break;
            }
        } catch (Exception e) {
        }
        return jsonObj;
    }

	/*
	 * private void performPublish(Activity activity, PendingAction action,
	 * boolean allowNoSession) { try { Session session =
	 * Session.getActiveSession(); // Session session =
	 * Session.openActiveSessionFromCache(activity); if(session != null) {
	 * pendingAction = action; if(hasPublishPermission()) { // We can do the
	 * action right away. handlePendingAction(); return; } else
	 * if(session.isOpened()) { // We need to get new permissions, then complete
	 * the action when we get called back. NewPermissionsRequest
	 * requestPermission = new NewPermissionsRequest(activity, PERMISSION); //
	 * set the request code, and check the callback of the user input in
	 * onActivityResult
	 * requestPermission.setRequestCode(REQUEST_CODE_PERMISSION_PUBLISH); //
	 * request permission
	 * session.requestNewPublishPermissions(requestPermission); return; } else {
	 * shareListener
	 * .onShareError(Configuration.getString(R.string.sharing_failed)); } }
	 *
	 * if(allowNoSession) { pendingAction = action; handlePendingAction(); } }
	 * catch (Exception e) { if(shareListener != null) {
	 * shareListener.onShareError
	 * (Configuration.getString(R.string.sharing_failed)); } } }
	 */

	/*
	 * public void shareImageViaFacebook(final Activity activity, final File
	 * photo, final String description) { try { Bitmap bi =
	 * BitmapFactory.decodeFile(photo.getPath()); ByteArrayOutputStream baos =
	 * new ByteArrayOutputStream(); bi.compress(Bitmap.CompressFormat.JPEG, 100,
	 * baos); byte[] data = baos.toByteArray();
	 *
	 * Bundle params = new Bundle(); params.putString("method",
	 * "photos.upload"); params.putByteArray("picture", data);
	 * params.putString("caption", "this is the caption");
	 * params.putString("description", "this is the description");
	 *
	 * @SuppressWarnings("deprecation") AsyncFacebookRunner mAsyncRunner = new
	 * AsyncFacebookRunner(facebook); mAsyncRunner.request(null, params, "POST",
	 * new SampleUploadListener(), null); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 */

    /**
     * Register a {@link FacebookProviderListener} This must be called before
     * initializing FacebookProviderListener
     *
     * @param listener
     */
    public void registerListener(FacebookProviderListener listener) {
        try {
            this.listener = listener;
        } catch (Exception e) {
        }
    }

    /**
     * Removes registered listener
     */
    public void unregisterListener() {
        try {
            listener = null;
        } catch (Exception e) {
        }
    }

    /**
     * Calls the onFacebookSessionOpened of a registered LoginProviderListener
     * and sends along the accessToken and the userId of the current Session
     *
     * @param accessToken
     */
    private void broadcastSessionOpened(String accessToken, String userId, HashMap<String, Object> map) {
        try {
            if (listener != null) {
                listener.onFacebookSessionOpened(accessToken, userId, map);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Calls the onFacebookSessionClosed of a registered LoginProviderListener
     */
    private void broadcastSessionClosed() {
        try {
            if (listener != null) {
                listener.onFacebookSessionClosed();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Calls the onFacebookException of a registered LoginProviderListener
     *
     * @param exception
     *            The Facebook exception received from the SDK
     */
    private void broadcastFacebookException(Exception exception) {
        try {
            if (listener != null) {
                listener.onFacebookException(exception);
            }
        } catch (Exception e) {
        }
    }

/*	private void broadcastFacebookFriends(
			ArrayList<AppFacebookFriend> arrayFriends,
			ArrayList<String> arrayActiveIds) {
		try {
			if (listener != null) {
				listener.onFacebookFriendsReceived(arrayFriends, arrayActiveIds);
			}
		} catch (Exception e) {
		}
	}*/

    private class AppSocialPendingAction {

        private Activity activity;
        private String desrciption;
        private String shareURL;
        private String pictureURL;
        private Bitmap bitmap;
        private STORY_TYPE storyType;

        public AppSocialPendingAction(Activity activity, String desrciption,
                                      String shareURL, String pictureURL, Bitmap bitmap,
                                      STORY_TYPE storType) {
            super();
            this.activity = activity;
            this.desrciption = desrciption;
            this.shareURL = shareURL;
            this.pictureURL = pictureURL;
            this.bitmap = bitmap;
            this.storyType = storType;
        }

        public Activity getActivity() {
            return activity;
        }

        public String getDesrciption() {
            return desrciption;
        }

        public String getShareURL() {
            return shareURL;
        }

        public String getPictureURL() {
            return pictureURL;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public STORY_TYPE getStoryType() {
            return storyType;
        }

    }
}