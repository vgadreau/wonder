package er.pdf;

import java.util.Map;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXResourceManager;
import er.extensions.appserver.ERXWOContext;

/**
 * ERPDFWrapper will render the containing component content as a PDF document.
 * The contained content must be valid XHTML markup suitable for processing by
 * the chosen rendering engine. ERPDFWrapper is intended to be the outer most
 * element on the page and should not have any trailing content or whitespace after
 * the closing tag.
 * 
 * @binding secure
 * @binding enabled
 * @binding filename
 * 
 * @author sharpy
 * @author q
 */
public class ERPDFWrapper extends WODynamicGroup implements WOActionResults {

  protected NSMutableDictionary<String, WOAssociation> _associations;
  protected WOAssociation _secure;
  protected WOAssociation _enabled;
  protected WOAssociation _filename;

  public ERPDFWrapper(String name, NSDictionary<String, WOAssociation> someAssociations, WOElement children) {
    super(name, someAssociations, children);
    _associations = someAssociations.mutableClone();
    _secure = _associations.removeObjectForKey("secure");
    _enabled = _associations.removeObjectForKey("enabled");
    _filename = _associations.removeObjectForKey("filename");
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {    
    boolean enabled = _enabled != null ? _enabled.booleanValueInComponent(context.component()) : true;

    super.appendToResponse(response, context);

    if (enabled) {
      boolean secure = _secure != null ? _secure.booleanValueInComponent(context.component()) : false;
      NSMutableDictionary<String, Object> config = new NSMutableDictionary<String, Object>();
      for (Map.Entry<String, WOAssociation> entry : _associations.entrySet()) {
        Object value = entry.getValue().valueInComponent(context.component());
        if (value != null)
          config.setObjectForKey(value, entry.getKey());
      }
      
      String resourceUrlPrefix = ERXResourceManager._completeURLForResource("", secure, context);
      NSData data = ERPDFUtilities.htmlAsPdf(response.contentString(), response.contentEncoding(), resourceUrlPrefix, config);

      String filename = _filename != null ? (String)_filename.valueInComponent(context.component()) : "result.pdf";

      response.setHeader("inline; filename=\"" + filename + "\"", "content-disposition");
      response.setHeader("application/pdf", "Content-Type");
      response.setHeader(String.valueOf(data.length()), "Content-Length");
      response.setContent(data);
    }
  }

  public WOResponse generateResponse() {
    WOContext context = ERXWOContext.currentContext();
    WOResponse response = WOApplication.application().createResponseInContext(context);
    appendToResponse(response, context);
    return response;
  }
}