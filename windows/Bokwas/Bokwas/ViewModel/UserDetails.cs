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
using System.Collections.Generic;
using System.IO.IsolatedStorage;
using System.Security.Cryptography;
using System.Text;
using System.Xml.Serialization;
using System.IO;
using System.Xml;
using System.Diagnostics;

namespace Bokwas.ViewModel
{
    public class UserDetails
    {
        /// <summary>
        /// User's first name
        /// </summary>
        private const string UserFirstNameKey = "user_first_name";

        /// <summary>
        /// User's last name
        /// </summary>
        private const string UserLastNameKey = "user_last_name";

        /// <summary>
        /// User's email ID
        /// </summary>
        private const string UserEmailIDKey = "user_email_id";

        /// <summary>
        /// User's gender
        /// </summary>
        private const string UserGenderKey = "user_gender";

        /// <summary>
        /// User's bokwas name
        /// </summary>
        private const string UserBokwasNameKey = "user_bokwas_name";

        /// <summary>
        /// User's friend list
        /// </summary>
        private const string UserFriendListKey = "user_friend_list";

        /// <summary>
        /// Tries to retrieve a Users Details
        /// </summary>
        /// <returns>
        /// A valid User Detail including first and last name, email ID, gender, bokwas name, friends list, or null (including if token already expired)
        /// </returns>
        public static UserDataModel Load()
        {
            // read first name
            string userFirstName = LoadEncryptedSettingValue(UserFirstNameKey);

            // read last name
            string userLastName = LoadEncryptedSettingValue(UserLastNameKey);

            // read gender
            string userGender = LoadEncryptedSettingValue(UserGenderKey);

            // read email ID
            string userEmailID = LoadEncryptedSettingValue(UserEmailIDKey);

            // read bokwas name
            string userBokwasName = LoadEncryptedSettingValue(UserBokwasNameKey);

            // read Friends List
            List<FriendDetails> friendsList = LoadEncryptedSettingValueList(UserFriendListKey);

            // return true only if first name and friends list is not null
            if (!string.IsNullOrWhiteSpace(userFirstName) && friendsList.Count!=0)
            {
                Debug.WriteLine("Reading from local store");
                return new UserDataModel()
                {
                    UserBokwasName = userBokwasName,
                    UserEmailID = userEmailID,
                    UserFirstName = userFirstName,
                    UserLastName = userLastName,
                    UserFriendList = friendsList,
                    UserGender = userGender
                };
            }
            else
            {
                return null;
            }
        }

        /// <summary>
        /// Saves the user data to a persistent storage after encryption.
        /// </summary>
        /// <param name="session">A valid UserDataModel object</param>
        public static void Save(UserDataModel userData)
        {
            SaveEncryptedSettingValue(UserFirstNameKey, userData.UserFirstName);
            SaveEncryptedSettingValue(UserLastNameKey, userData.UserLastName);
            SaveEncryptedSettingValue(UserGenderKey, userData.UserGender);
            SaveEncryptedSettingValue(UserBokwasNameKey, userData.UserBokwasName);
            SaveEncryptedSettingValue(UserEmailIDKey, userData.UserEmailID);
            SaveEncryptedSettingValue(UserFriendListKey, userData.UserFriendList);
        }

        /// <summary>
        /// Removes saved values for a users details
        /// </summary>
        public static void Remove()
        {
            RemoveEncryptedSettingValue(UserFirstNameKey);
            RemoveEncryptedSettingValue(UserLastNameKey);
            RemoveEncryptedSettingValue(UserGenderKey);
            RemoveEncryptedSettingValue(UserBokwasNameKey);
            RemoveEncryptedSettingValue(UserEmailIDKey);
            RemoveEncryptedSettingValue(UserFriendListKey);
        }

        /// <summary>
        /// Removes an encrypted setting value
        /// </summary>
        /// <param name="key">Key to remove</param>
        private static void RemoveEncryptedSettingValue(string key)
        {
            if (IsolatedStorageSettings.ApplicationSettings.Contains(key))
            {
                IsolatedStorageSettings.ApplicationSettings.Remove(key);
                IsolatedStorageSettings.ApplicationSettings.Save();
            }
        }

        /// <summary>
        /// Loads an encrypted setting value for a given key
        /// </summary>
        /// <param name="key">The key to load</param>
        /// <returns>
        /// The value of the key
        /// </returns>
        /// <exception cref="KeyNotFoundException">The given key was not found</exception>
        private static string LoadEncryptedSettingValue(string key)
        {
            string value = null;
            if (IsolatedStorageSettings.ApplicationSettings.Contains(key))
            {
                var protectedBytes = IsolatedStorageSettings.ApplicationSettings[key] as byte[];
                if (protectedBytes != null)
                {
                    byte[] valueBytes = ProtectedData.Unprotect(protectedBytes, null);
                    value = Encoding.UTF8.GetString(valueBytes, 0, valueBytes.Length);
                }
            }

            return value;
        }

        /// <summary>
        /// Loads an encrypted setting value list for a given key
        /// </summary>
        /// <param name="key">The key to load</param>
        /// <returns>
        /// The value of the key
        /// </returns>
        /// <exception cref="KeyNotFoundException">The given key was not found</exception>
        private static List<FriendDetails> LoadEncryptedSettingValueList(string key)
        {
            List<FriendDetails> list = new List<FriendDetails>();
            string value;
            if (IsolatedStorageSettings.ApplicationSettings.Contains(key))
            {
                var protectedBytes = IsolatedStorageSettings.ApplicationSettings[key] as byte[];
                if (protectedBytes != null)
                {
                    byte[] valueBytes = ProtectedData.Unprotect(protectedBytes, null);
                    value = Encoding.UTF8.GetString(valueBytes, 0, valueBytes.Length);
                    string[] friendDetailList = value.Split('%');
                    foreach (string s in friendDetailList)
                    {
                        string[] friendDetails = s.Split(';');
                        if (friendDetails.Length < 5)
                        {
                            continue;
                        }
                        string bokwasName = null;
                        string bokwasID = null;
                        if (friendDetails[4] != "#")
                            bokwasName = friendDetails[4];

                        if (friendDetails[3] != "#")
                            bokwasID = friendDetails[3];
                        FriendDetails fd = new FriendDetails(friendDetails[0], friendDetails[2], friendDetails[1], bokwasName, bokwasID);
                        list.Add(fd);
                    }
                    
                }
            }
            return list;
        }

        /// <summary>
        /// Saves a setting value against a given key, encrypted
        /// </summary>
        /// <param name="key">The key to save against</param>
        /// <param name="value">The value to save against</param>
        /// <exception cref="System.ArgumentOutOfRangeException">The key or value provided is unexpected</exception>
        private static void SaveEncryptedSettingValue(string key, string value)
        {
            if (!string.IsNullOrWhiteSpace(key) && !string.IsNullOrWhiteSpace(value))
            {
                byte[] valueBytes = Encoding.UTF8.GetBytes(value);

                // Encrypt the value by using the Protect() method.
                byte[] protectedBytes = ProtectedData.Protect(valueBytes, null);
                if (IsolatedStorageSettings.ApplicationSettings.Contains(key))
                {
                    IsolatedStorageSettings.ApplicationSettings[key] = protectedBytes;
                }
                else
                {
                    IsolatedStorageSettings.ApplicationSettings.Add(key, protectedBytes);
                }

                IsolatedStorageSettings.ApplicationSettings.Save();
            }
            else
            {
                throw new ArgumentOutOfRangeException();
            }
        }

        private static void SaveEncryptedSettingValue(string key, List<FriendDetails> list)
        {  
            if (!string.IsNullOrWhiteSpace(key) && list.Count!=0)
            {
                var xml = list.Count + "%";
                foreach (FriendDetails fd in list)
                {
                    xml += fd.getFbName() + ";" + fd.getFbPicLink() + ";" + fd.getId() + ";";
                    if (fd.getBokwasAvatarId() == null)
                    {
                        xml += "#;";
                    }
                    else
                    {
                        xml += fd.getBokwasAvatarId() + ";";
                    }
                    if (fd.getBokwasName() == null)
                    {
                        xml += "#%";
                    }
                    else
                    {
                        xml += fd.getBokwasName() + "%";
                    }
                }
                byte[] valueBytes = Encoding.UTF8.GetBytes(xml);
                // Encrypt the value by using the Protect() method.
                byte[] protectedBytes = ProtectedData.Protect(valueBytes, null);
                if (IsolatedStorageSettings.ApplicationSettings.Contains(key))
                {
                    IsolatedStorageSettings.ApplicationSettings[key] = protectedBytes;
                }
                else
                {
                    IsolatedStorageSettings.ApplicationSettings.Add(key, protectedBytes);
                }

                IsolatedStorageSettings.ApplicationSettings.Save();
            }
            else
            {
                throw new ArgumentOutOfRangeException();
            }
        }
    }
}
