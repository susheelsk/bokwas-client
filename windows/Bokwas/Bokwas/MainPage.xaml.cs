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
using Facebook.Client;
using Bokwas.ViewModel;
using System.Threading;
using System.Windows.Controls.Primitives;
using System.ComponentModel;
using Microsoft.Phone.Shell;

namespace Bokwas
{
    public partial class MainPage : PhoneApplicationPage
    {
        // Constructor
        public MainPage()
        {
            InitializeComponent();
            textBlock2.Text="Initializing";
            btnFacebookLogin.Visibility = Visibility.Collapsed;
            var indicator = new ProgressIndicator
            {
                IsVisible = true,
                IsIndeterminate = true,
                Text = "Logging in..."
            };
            SystemTray.SetProgressIndicator(this, indicator);

            Loaded += (s, e) =>
            {
                FacebookSession fbSession = SessionStorage.Load();
                if (fbSession != null)
                {
                    var url = string.Format("/Pages/FacebookInfoPage.xaml");
                    NavigationService.Navigate(new Uri(url, UriKind.Relative));
                }
                else
                {
                    indicator.IsVisible = false;
                    textBlock2.Text = "";
                    btnFacebookLogin.Visibility = Visibility.Visible;
                }
            };
        }

        private void btnFacebookLogin_Click(object sender, RoutedEventArgs e)
        {
            NavigationService.Navigate(new Uri("/Pages/FacebookLoginPage.xaml", UriKind.Relative));
        }

       
    }
}