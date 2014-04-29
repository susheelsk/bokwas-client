using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Microsoft.Phone.Controls;
using System.Windows.Media.Imaging;
using System.Threading.Tasks;
using Facebook;
using System.Diagnostics;
using Bokwas.ViewModel;
using Newtonsoft.Json.Linq;
using Facebook.Client;
using Microsoft.Phone.Shell;

namespace Bokwas.Pages
{
    public partial class FacebookInfoPage : PhoneApplicationPage
    {
        private string _accessToken;
        private string _userId;
        private string _firstName;
        private string _lastName;
        private string _gender;
        private string _email;
        private List<FriendDetails> _friendList = new List<FriendDetails>();
        private UserDataModel _userData = new UserDataModel();

        public FacebookInfoPage()
        {
            InitializeComponent();
            var indicator = new ProgressIndicator
            {
                IsVisible = true,
                IsIndeterminate = true,
                Text = "Fetching User Details..."
            };
            SystemTray.SetProgressIndicator(this, indicator);
            _accessToken = null;
            _userId = null;
            _firstName = null;
            _lastName = null;
            _gender = null;
            _email = null;
            Loaded += (s, e) =>
           {
               FacebookSession fbSession = SessionStorage.Load();
               indicator.Text = "Fetching Friend Details...";
               _accessToken = fbSession.AccessToken;
               _userId = fbSession.FacebookId;
               LoadFacebookData();
               var url = string.Format("/Pages/AvatarSelector.xaml");
               Dispatcher.BeginInvoke(() => NavigationService.Navigate(new Uri(url, UriKind.Relative)));
           };
        }


        private void LoadFacebookData()
        {
            if (UserDetails.Load() != null)
            {
                Debug.WriteLine("Friend details are in local store");
                //DisplayUserData();
            }
            else
            {
                FetchUserInfo();
            }
        }

        private void DisplayUserData()
        {
            UserDataModel ud = UserDetails.Load();
            Debug.WriteLine(ud.UserFirstName);
            Debug.WriteLine(ud.UserLastName);
            foreach (FriendDetails f in ud.UserFriendList)
            {
                Debug.WriteLine(f.getFbName());
            }
        }

        private void FetchUserInfo()
        {
            var fb = new FacebookClient(_accessToken);
            textBlock2.Text = _userId;
            textBlock1.Text = _accessToken;
            fb.GetCompleted += (o, e) =>
            {
                if (e.Error != null)
                {
                    Dispatcher.BeginInvoke(() => MessageBox.Show(e.Error.Message));
                    return;
                }

                var result = (IDictionary<string, object>)e.GetResultData();

                Dispatcher.BeginInvoke(() =>
                {
                    textBlock1.Text = "Over here";
                    _firstName = (string)result["first_name"];
                   _lastName = (string)result["last_name"];
                    _gender = (string)result["gender"];
                    _email = (string)result["email"];
                    loadFriendsList();
                    _userData.UserFirstName=_firstName;
                    _userData.UserLastName=_lastName;
                    _userData.UserGender=_gender;
                    _userData.UserEmailID=_email;
                    _userData.UserBokwasName="0";
                    
                });
            };

            fb.GetAsync("me");
        }

       private void loadFriendsList()
        {
            var fb = new FacebookClient(_accessToken);

            fb.GetCompleted += (o, e) =>
            {
                if (e.Error != null)
                {
                    Dispatcher.BeginInvoke(() => MessageBox.Show(e.Error.Message));
                    return;
                }

                var result = (IDictionary<string, object>)e.GetResultData();
                var data = (IList<object>)result["data"];
                var count = data.Count;
                

                // since this is an async callback, make sure to be on the right thread
                // when working with the UI.
                Dispatcher.BeginInvoke(() =>
                {
                    String TotalFriends;
                    TotalFriends = string.Format("You have {0} friend(s).", count);
                    Debug.WriteLine(TotalFriends);
                    foreach (IDictionary<string, object> d in data)
                    {
                        _friendList.Add(new FriendDetails((string)d["name"], (string)d["uid"], (string)d["pic_square"], null, null));
                    }
                    _userData.UserFriendList = _friendList;
                    UserDetails.Save(_userData);
                });
            };

            // query to get all the friends
            var query = string.Format("SELECT uid,name,pic_square FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1={0})", "me()");

            // Note: For windows phone 7, make sure to add [assembly: InternalsVisibleTo("Facebook")] if you are using anonymous objects as parameter.
            fb.GetAsync("fql", new { q = query });
        }

        }

}