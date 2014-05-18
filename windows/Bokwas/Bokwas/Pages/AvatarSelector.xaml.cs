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
using Microsoft.Phone.Controls.Primitives;

namespace Bokwas.Pages
{
    public partial class AvatarSelector : PhoneApplicationPage
    {
        // Constructor
		public AvatarSelector()
		{
			InitializeComponent();
			List<CountryData> data = new List<CountryData>();
			data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_1.png", UriKind.Relative).ToString(), ID = 1 });
			data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_2.png", UriKind.Relative).ToString(), ID = 2 });
			data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_3.png", UriKind.Relative).ToString(), ID = 3 });
			data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_4.png", UriKind.Relative).ToString(), ID = 4 });
			data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_5.png", UriKind.Relative).ToString(), ID = 5 });
		    data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_6.png", UriKind.Relative).ToString(), ID = 6 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_7.png", UriKind.Relative).ToString(), ID = 7 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_8.png", UriKind.Relative).ToString(), ID = 8 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_9.png", UriKind.Relative).ToString(), ID = 9 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_10.png", UriKind.Relative).ToString(), ID = 10 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_11.png", UriKind.Relative).ToString(), ID = 11 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_12.png", UriKind.Relative).ToString(), ID = 12 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_13.png", UriKind.Relative).ToString(), ID = 13 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_14.png", UriKind.Relative).ToString(), ID = 14 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_15.png", UriKind.Relative).ToString(), ID = 15 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_16.png", UriKind.Relative).ToString(), ID = 16 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_17.png", UriKind.Relative).ToString(), ID = 17 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_18.png", UriKind.Relative).ToString(), ID = 18 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_19.png", UriKind.Relative).ToString(), ID = 19 });
            data.Add(new CountryData() { Name = "", Flag = new Uri(@"../Assets/Avatars/avatar_20.png", UriKind.Relative).ToString(), ID = 20 });
			this.selectorLeft.DataSource = new ListLoopingDataSource<CountryData>() { Items = data, SelectedItem = data[2] };
		}

		// option 2: implement and use IComparer<T>
		public class CityDataComparer : IComparer<CityData>
		{
			#region IComparer<CityData> Members

			public int Compare(CityData x, CityData y)
			{
				return x.ID.CompareTo(y.ID);
			}

			#endregion
		}

		public class CityData 
		{
			public string Name
			{
				get;
				set;
			}

			public string Country
			{
				get;
				set;
			}

			public int ID
			{
				get;
				set;
			}
		}

		// option 1: implement IComparable<T>
		public class CountryData : IComparable<CountryData>
		{
			public string Name
			{
				get;
				set;
			}

			public string Flag
			{
				get;
				set;
			}
			public int ID
			{
				get;
				set;
			}

			#region IComparable<CityData> Members

			public int CompareTo(CountryData other)
			{
				return this.ID.CompareTo(other.ID);
			}

			#endregion
		}

		// abstract the reusable code in a base class
		// this will allow us to concentrate on the specifics when implementing deriving looping data source classes
		public abstract class LoopingDataSourceBase : ILoopingSelectorDataSource
		{
			private object selectedItem;

			#region ILoopingSelectorDataSource Members

			public abstract object GetNext(object relativeTo);

			public abstract object GetPrevious(object relativeTo);

			public object SelectedItem
			{
				get
				{
					return this.selectedItem;
				}
				set
				{
					// this will use the Equals method if it is overridden for the data source item class
					if (!object.Equals(this.selectedItem, value))
					{
						// save the previously selected item so that we can use it 
						// to construct the event arguments for the SelectionChanged event
						object previousSelectedItem = this.selectedItem;
						this.selectedItem = value;
						// fire the SelectionChanged event
						this.OnSelectionChanged(previousSelectedItem, this.selectedItem);
					}
				}
			}

			public event EventHandler<SelectionChangedEventArgs> SelectionChanged;

			protected virtual void OnSelectionChanged(object oldSelectedItem, object newSelectedItem)
			{
				EventHandler<SelectionChangedEventArgs> handler = this.SelectionChanged;
				if (handler != null)
				{
					handler(this, new SelectionChangedEventArgs(new object[] { oldSelectedItem }, new object[] { newSelectedItem }));
				}
			}

			#endregion
		}

		public class ListLoopingDataSource<T> : LoopingDataSourceBase
		{
			private LinkedList<T> linkedList;
			private List<LinkedListNode<T>> sortedList;
			private IComparer<T> comparer;
			private NodeComparer nodeComparer;

			public ListLoopingDataSource()
			{
			}

			public IEnumerable<T> Items
			{
				get
				{
					return this.linkedList;
				}
				set
				{
					this.SetItemCollection(value);
				}
			}

			private void SetItemCollection(IEnumerable<T> collection)
			{
				this.linkedList = new LinkedList<T>(collection);

				this.sortedList = new List<LinkedListNode<T>>(this.linkedList.Count);
				// initialize the linked list with items from the collections
				LinkedListNode<T> currentNode = this.linkedList.First;
				while (currentNode != null)
				{
					this.sortedList.Add(currentNode);
					currentNode = currentNode.Next;
				}

				IComparer<T> comparer = this.comparer;
				if (comparer == null)
				{
					// if no comparer is set use the default one if available
					if (typeof(IComparable<T>).IsAssignableFrom(typeof(T)))
					{
						comparer = Comparer<T>.Default;
					}
					else
					{
						throw new InvalidOperationException("There is no default comparer for this type of item. You must set one.");
					}
				}

				this.nodeComparer = new NodeComparer(comparer);
				this.sortedList.Sort(this.nodeComparer);
			}

			public IComparer<T> Comparer
			{
				get
				{
					return this.comparer;
				}
				set
				{
					this.comparer = value;
				}
			}

			public override object GetNext(object relativeTo)
			{
				// find the index of the node using binary search in the sorted list
				int index = this.sortedList.BinarySearch(new LinkedListNode<T>((T)relativeTo), this.nodeComparer);
				if (index < 0)
				{
					return default(T);
				}

				// get the actual node from the linked list using the index
				LinkedListNode<T> node = this.sortedList[index].Next;
				if (node == null)
				{
					// if there is no next node get the first one
					node = this.linkedList.First;
				}
				return node.Value;
			}

			public override object GetPrevious(object relativeTo)
			{
				int index = this.sortedList.BinarySearch(new LinkedListNode<T>((T)relativeTo), this.nodeComparer);
				if (index < 0)
				{
					return default(T);
				}
				LinkedListNode<T> node = this.sortedList[index].Previous;
				if (node == null)
				{
					// if there is no previous node get the last one
					node = this.linkedList.Last;
				}
				return node.Value;
			}

			private class NodeComparer : IComparer<LinkedListNode<T>>
			{
				private IComparer<T> comparer;

				public NodeComparer(IComparer<T> comparer)
				{
					this.comparer = comparer;
				}

				#region IComparer<LinkedListNode<T>> Members

				public int Compare(LinkedListNode<T> x, LinkedListNode<T> y)
				{
					return this.comparer.Compare(x.Value, y.Value);
				}

				#endregion
			}

		}
    }
}