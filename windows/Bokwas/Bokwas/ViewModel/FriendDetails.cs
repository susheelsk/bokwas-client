using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;

namespace Bokwas.ViewModel
{
    public class FriendDetails
    {
        private String fbName {set; get; }
	    private String id {set; get; }
        private String fbPicLink { set; get; }
        private String bokwasName { set; get; }
        private String bokwasAvatarId { set; get; }
	
	    public String getFbName() {
		    return fbName;
	    }

	    public void setFbName(String fbName) {
		    this.fbName = fbName;
	    }

	    public String getId() {
		    return id;
	    }

	    public void setId(String id) {
		    this.id = id;
	    }

	    public String getFbPicLink() {
		    return fbPicLink;
	    }

	    public void setFbPicLink(String fbPicLink) {
		    this.fbPicLink = fbPicLink;
	    }

	    public String getBokwasName() {
		    return bokwasName;
	    }

	    public void setBokwasName(String bokwasName) {
		    this.bokwasName = bokwasName;
	    }

	    public String getBokwasAvatarId() {
		    return bokwasAvatarId;
	    }

	    public void setBokwasAvatarId(String bokwasAvatarId) {
		    this.bokwasAvatarId = bokwasAvatarId;
	    }

	    public FriendDetails(String fbName, String id, String fbPicLink,
			String bokwasName, String bokwasAvatarId) {
		    this.fbName = fbName;
		    this.id = id;
		    this.fbPicLink = fbPicLink;
		    this.bokwasName = bokwasName;
		    this.bokwasAvatarId = bokwasAvatarId;
	}
    }
}
