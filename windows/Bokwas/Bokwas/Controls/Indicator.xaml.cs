using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Shapes;

namespace ImageCarousel.Controls
{
    public partial class Indicator : UserControl
    {
        /// <summary>
        /// Public ItemsCount property of type DependencyProperty
        /// </summary>
        public static readonly DependencyProperty ItemsCountProperty =
        DependencyProperty.Register("ItemsCount",
            typeof(int),
            typeof(Indicator),
            new PropertyMetadata(OnItemsCountChanged));

        /// <summary>
        /// Public SelectedPivotIndex property of type DependencyProperty
        /// </summary>
        public static readonly DependencyProperty SelectedPivotIndexProperty =
        DependencyProperty.Register("SelectedPivotIndex",
            typeof(int),
            typeof(Indicator),
            new PropertyMetadata(OnPivotIndexChanged));

        /// <summary>
        /// Constructor
        /// </summary>
        public Indicator()
        {
            InitializeComponent();
        }

        /// <summary>
        /// Gets or sets number of pivot items
        /// </summary>
        public int ItemsCount
        {
            set { SetValue(ItemsCountProperty, value); }
            get { return (int)GetValue(ItemsCountProperty); }
        }

        /// <summary>
        /// Gets or sets index of selected pivot item
        /// </summary>
        public int SelectedPivotIndex
        {
            set { SetValue(SelectedPivotIndexProperty, value); }
            get { return (int)GetValue(SelectedPivotIndexProperty); }
        }

        /// <summary>
        /// OnItemsCountChanged property-changed handler 
        /// </summary>
        private static void OnItemsCountChanged(DependencyObject obj, DependencyPropertyChangedEventArgs args)
        {
            (obj as Indicator).SetRectangles();
        }

        /// <summary>
        /// OnPivotIndexChanged property-changed handler 
        /// </summary>
        private static void OnPivotIndexChanged(DependencyObject obj, DependencyPropertyChangedEventArgs args)
        {
            (obj as Indicator).AccentRectangle();
        }

        /// <summary>
        /// Draws rectangles.
        /// </summary>
        private void SetRectangles()
        {
            IndicatorPanel.Children.Clear();
            for (int i = 0; i < this.ItemsCount; i++)
            {
                Rectangle rectangle = new Rectangle() { Height = 9, Width = 9, Margin = new Thickness(4, 0, 0, 0) };
                IndicatorPanel.Children.Add(rectangle);
            }
            this.AccentRectangle();
        }

        /// <summary>
        /// Accents selected pivot item rectangle.
        /// </summary>
        private void AccentRectangle()
        {
            int i = 0;
            foreach (var item in IndicatorPanel.Children)
            {
                if (item is Rectangle)
                {
                    Rectangle rectangle = (Rectangle)item;
                    if (i == this.SelectedPivotIndex)
                        rectangle.Fill = (SolidColorBrush)Application.Current.Resources["PhoneForegroundBrush"];
                    else
                        rectangle.Fill = (SolidColorBrush)Application.Current.Resources["PhoneDisabledBrush"];
                    i++;
                }
            }
        }
    }
}
