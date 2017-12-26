package com.suifeng.app.smartmusic.utils;


import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.suifeng.app.smartmusic.entity.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactUtil {
    /**
     * 按号码-拼音搜索联系人
     */
    public static ArrayList<Contact> search(final String str,
                                            final ArrayList<Contact> allContacts) {
        ArrayList<Contact> contactList = new ArrayList<Contact>();
        // 如果搜索条件以0 1 +开头则按号码搜索
        if (str.startsWith("0") || str.startsWith("1") || str.startsWith("+")) {
            for (Contact contact : allContacts) {
                if (contact.getNumber() != null && contact.getName() != null) {
                    if (contact.getNumber().contains(str)
                            || contact.getName().contains(str)) {
                        contact.setGroup(str);
                        contactList.add(contact);
                    }
                }
            }
            return contactList;
        }

        // final ChineseSpelling finder = ChineseSpelling.getInstance();
        // finder.setResource(str);
        // final String result = finder.getSpelling();
        // 先将输入的字符串转换为拼音
        // final String result = PinYinUtil.getFullSpell(str);
        final String result = PinYin.getPinYin(str);
        for (Contact contact : allContacts) {
            if (contains(contact, result)) {
                contactList.add(contact);
            }
        }

        return contactList;
    }

    /**
     * 根据拼音搜索
     */
    public static boolean contains(Contact contact, String search) {
        if (TextUtils.isEmpty(contact.getName()) || TextUtils.isEmpty(search)) {
            return false;
        }

        boolean flag = false;

        // 简拼匹配,如果输入在字符串长度大于6就不按首字母匹配了
        if (search.length() < 6) {
            // String firstLetters = FirstLetterUtil.getFirstLetter(contact
            // .getName());
            // 获得首字母字符串
            String firstLetters = UnicodeGBK2Alpha
                    .getSimpleCharsOfString(contact.getName());
            // String firstLetters =
            // PinYinUtil.getFirstSpell(contact.getName());
            // 不区分大小写
            Pattern firstLetterMatcher = Pattern.compile("^" + search,
                    Pattern.CASE_INSENSITIVE);
            flag = firstLetterMatcher.matcher(firstLetters).find();
        }

        if (!flag) { // 如果简拼已经找到了，就不使用全拼了
            // 全拼匹配
            // ChineseSpelling finder = ChineseSpelling.getInstance();
            // finder.setResource(contact.getName());
            // 不区分大小写
            Pattern pattern2 = Pattern
                    .compile(search, Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(PinYin.getPinYin(contact
                    .getName()));
            flag = matcher2.find();
        }

        return flag;
    }

    public static ArrayList<Contact> getAllContacts(ContentResolver contentResolver) {
        ArrayList<Contact> contacts = new ArrayList<>();
        String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                cols, null, null, null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            // 取得联系人名字
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String name = cursor.getString(nameFieldColumnIndex);
            String number = cursor.getString(numberFieldColumnIndex);
            Contact contact = new Contact(name, number);
            contacts.add(contact);
        }
        return contacts;
    }
}
