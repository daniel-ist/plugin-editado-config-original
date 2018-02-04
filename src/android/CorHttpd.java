package com.rjfun.cordova.httpd;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

//import org.apache.cordova.*;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import android.util.Log;
import android.content.Context;
import android.content.res.AssetManager;

/**
 * This class echoes a string called from JavaScript.
 */
public class CorHttpd extends CordovaPlugin {
    // Coloquei por causa do metodo onRequest():
    public CallbackContext onRequestCallbackContext;
    public HashMap<String, Object> responses;
    public NanoHTTPD nanoHTTPD;

    /** Common tag used for logging statements. */
    private static final String LOGTAG = "CorHttpd";
    
    /** Cordova Actions. */
    private static final String ACTION_START_SERVER = "startServer";
    private static final String ACTION_STOP_SERVER = "stopServer";
    private static final String ACTION_GET_URL = "getURL";
    private static final String ACTION_GET_LOCAL_PATH = "getLocalPath";
    private static final String ACTION_ON_REQUEST = "onRequest";
    private static final String ACTION_SEND_RESPONSE = "sendResponse";

    private static final String OPT_WWW_ROOT = "www_root";
    private static final String OPT_PORT = "port";
    private static final String OPT_LOCALHOST_ONLY = "localhost_only";

    private String www_root = "";
	private int port = 8888;
	private boolean localhost_only = false;

	private String localPath = "";
	public WebServer server = null;
	private String	url = "";

    //tirei do codigo do plugin nodejs
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.responses = new HashMap<String, Object>();
    }


//    @Override
    public boolean execute_new(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (ACTION_START_SERVER.equals(action)) {
	        PluginResult result = this.startServer(args, callbackContext);
            callbackContext.sendPluginResult( result );
            return true;
        }
        else if (ACTION_STOP_SERVER.equals(action)) {
        PluginResult result = this.stopServer(args, callbackContext);
            callbackContext.sendPluginResult( result );
            return true;
        }
        else if (ACTION_ON_REQUEST.equals(action)) {
            this.onRequest(args, callbackContext);
            return true;
        }
        else if (ACTION_SEND_RESPONSE.equals(action)) {
            this.sendResponse(args, callbackContext);
            return true;
        }
        else if (ACTION_GET_URL.equals(action)) {
            PluginResult result =  this.getURL(args, callbackContext);
            callbackContext.sendPluginResult( result );
            return true;
        }

        return false;  // Returning false results in a "MethodNotFound" error.
    }




    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;
        if (ACTION_START_SERVER.equals(action)) {
            result = startServer(inputs, callbackContext);
            
        } else if (ACTION_STOP_SERVER.equals(action)) {
            result = stopServer(inputs, callbackContext);
            
        } else if (ACTION_GET_URL.equals(action)) {
            result = getURL(inputs, callbackContext);
            
        } else if (ACTION_GET_LOCAL_PATH.equals(action)) {
            result = getLocalPath(inputs, callbackContext);
            
        }else if (ACTION_ON_REQUEST.equals(action)) {
            this.onRequest(inputs, callbackContext);
            return true;
            
        }else if (ACTION_SEND_RESPONSE.equals(action)) {
            this.sendResponse(inputs, callbackContext);
            return true;
        }else {
             Log.d(LOGTAG, String.format("Invalid action passed: %s", action));
             result = new PluginResult(Status.INVALID_ACTION);
         }
        
        if(result != null) {
            callbackContext.sendPluginResult( result );
        }
    return true;
    }



    
    private String __getLocalIpAddress() {
    	try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (! inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) {
                            String ip = inetAddress.getHostAddress();
                    		Log.w(LOGTAG, "local IP: "+ ip);
                    		return ip;
                    	}
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(LOGTAG, ex.toString());
        }
    	
		return "127.0.0.1";
    }



    private PluginResult startServer_broken(JSONArray inputs, CallbackContext callbackContext) {
		Log.w(LOGTAG, "startServer");

        JSONObject options = inputs.optJSONObject(0);
        if(options == null) return null;
        
        www_root = options.optString(OPT_WWW_ROOT);
        port = options.optInt(OPT_PORT, 8888);
        localhost_only = options.optBoolean(OPT_LOCALHOST_ONLY, false);
        
        if(www_root.startsWith("/")) {
    		//localPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        	localPath = www_root;
        } else {
        	//localPath = "file:///android_asset/www";
        	localPath = "www";
        	if(www_root.length()>0) {
        		localPath += "/";
        		localPath += www_root;
        	}
        }

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
            public void run() {
				String errmsg = __startServer();
				if (errmsg.length() > 0) {
					delayCallback.error( errmsg );
				} else {
                    if (localhost_only) {
                        url = "http://127.0.0.1:" + port;
                    } else {
                        url = "http://" + __getLocalIpAddress() + ":" + port;
                    }

			         PluginResult res = new PluginResult(PluginResult.Status.OK);
		            delayCallback.sendPluginResult( res);

	                delayCallback.success( url );
				}
            }
        });
        return null;
    }


    private PluginResult startServer(JSONArray inputs, CallbackContext callbackContext) {
        Log.w(LOGTAG, "startServer");

        JSONObject options = inputs.optJSONObject(0);
        if(options == null) return null;

        www_root = options.optString(OPT_WWW_ROOT);
        port = options.optInt(OPT_PORT, 8888);
        localhost_only = options.optBoolean(OPT_LOCALHOST_ONLY, false);

        if(www_root.startsWith("/")) {
            //localPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            localPath = www_root;
        } else {
            //localPath = "file:///android_asset/www";
            localPath = "www";
            if(www_root.length()>0) {
                localPath += "/";
                localPath += www_root;
            }
        }

        final CallbackContext delayCallback = callbackContext;

        String errmsg = __startServer();
        if (errmsg.length() > 0) {


        } else {
            if (localhost_only) {
                url = "http://127.0.0.1:" + port;
            } else {
                url = "http://" + __getLocalIpAddress() + ":" + port;
            }

            PluginResult res = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult( res);

        }
        return null;
    }


    private String __startServer() {

        Log.d(this.getClass().getName(), "daniel Entrou no startServer corhttpd.java");

    	String errmsg = "";
    	try {
    		AndroidFile f = new AndroidFile(localPath);
    		
	        Context ctx = cordova.getActivity().getApplicationContext();
			AssetManager am = ctx.getResources().getAssets();
    		f.setAssetManager( am );
    		
    		if(localhost_only) {
    			InetSocketAddress localAddr = new InetSocketAddress(InetAddress.getByAddress(new byte[]{127,0,0,1}), port);
    			server = new WebServer(localAddr, f, this);
    		} else {
    			server = new WebServer(port, f, this);
    		}
        	Log.d(this.getClass().getName(), "daniel criou com sucesso htpd");


        		//vou por aqui new nanohttpd(...)

        	//nanoHTTPD = new NanoHTTPD( port, f , this);
			Log.d(this.getClass().getName(), "danielLog criou nanohttpd com sucesso linha: 234 corhttpd.java");

		} catch (IOException e) {
			errmsg = String.format("IO Exception: %s", e.getMessage());
			Log.w(LOGTAG, errmsg);
	
		}

    	return errmsg;
    }

    private void __stopServer() {
		if (server != null) {
			server.stop();
			server = null;
		}
    }
    
   private PluginResult getURL(JSONArray inputs, CallbackContext callbackContext) {
		Log.w(LOGTAG, "getURL");
		
    	callbackContext.success( this.url );
        return null;
    }

    private PluginResult getLocalPath(JSONArray inputs, CallbackContext callbackContext) {
		Log.w(LOGTAG, "getLocalPath");
		
    	callbackContext.success( this.localPath );
        return null;
    }

    private PluginResult stopServer(JSONArray inputs, CallbackContext callbackContext) {
		Log.w(LOGTAG, "stopServer");
		
        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
            public void run() {
				__stopServer();
				url = "";
				localPath = "";
                delayCallback.success();
            }
        });
        
        return null;
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking		Flag indicating if multitasking is turned on for app
     */
    public void onPause(boolean multitasking) {
    	//if(! multitasking) __stopServer();
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking		Flag indicating if multitasking is turned on for app
     */
    public void onResume(boolean multitasking) {
    	//if(! multitasking) __startServer();
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    public void onDestroy() {
    	__stopServer();
    }

    /**
     * Just register the onRequest and send no result. This is needed to save the callbackContext to
     * invoke it later
     * @param args
     * @param callbackContext
     */
    private void onRequest(JSONArray args, CallbackContext callbackContext) {
        this.onRequestCallbackContext = callbackContext;
        Log.d(this.getClass().getName(), "danielLog onrequest (CorHttpd.java): " + args.toString() + onRequestCallbackContext);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        this.onRequestCallbackContext.sendPluginResult(pluginResult);
    }


     /**
     * Will be called if the js context sends an response to the webserver
     * @param args {UUID: {...}}
     * @param callbackContext
     * @throws JSONException
     */
    private void sendResponse(JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(this.getClass().getName(), "danielLog    Got sendResponse: " + args.toString() + callbackContext);
        this.responses.put(args.getString(0), args.get(1));
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }
}