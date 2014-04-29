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
using Microsoft.Phone.Shell;

namespace Bokwas
{
    public partial class SplashScreen : PhoneApplicationPage
    {
        public SplashScreen()
        {
            InitializeComponent();
            var indicator = new ProgressIndicator
            {
                IsVisible = true,
                IsIndeterminate = true,
                Text = "Testing!"
            };
            SystemTray.SetProgressIndicator(this, indicator);
        }
    }
}