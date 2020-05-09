package com.youtushuju.lingdongapp.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileBrowser {
    private String m_currentPath;
    private Set<String> m_history;
    private List<FileModel> m_fileList;
    private int m_filter;
    private int m_order;
    private List<String> m_extensions;
    private boolean m_showHidden = true;
    private FileBrowserCurrentChangedListener m_onCurrentChangedListener;

    public FileBrowser()
    {
        this(System.getProperty("user.home"));
    }

    public FileBrowser(String path)
    {
        m_history = new HashSet<String>();
        m_fileList = new ArrayList<FileModel>();
        m_filter = 0;
        m_order = 0;
        m_extensions = new ArrayList<String>();
        m_showHidden = true;
        if(path != null && !path.isEmpty())
            SetCurrentPath(path);
    }

    protected boolean ListFiles(String path)
    {
        File dir;
        File files[];
        FileModel item;

        dir = new File(path);
        if(!dir.isDirectory())
            return false;

        // TODO: 触发两次监听, 需要添加返回动作
        /*m_fileList.clear();
        if(m_onCurrentChangedListener != null)
            m_onCurrentChangedListener.OnCurrentChanged(this, false);*/

        files = dir.listFiles();
        if(files == null)
        {
            // 当目录无法访问时也更新当前路径
            if(m_currentPath != path)
            {
                m_currentPath = path;
                if(m_onCurrentChangedListener != null)
                    m_onCurrentChangedListener.OnCurrentChanged(this, FileBrowserCurrentChangedListener.ID_FILE_BROWSER_CURRENT_CHANGE_PATH);
            }
            return false;
        }

        // listFiles函数可能返回null返回, 而导致无法调用监听函数, 在这里清空文件列表
        m_fileList.clear();
        if(m_onCurrentChangedListener != null)
            m_onCurrentChangedListener.OnCurrentChanged(this, FileBrowserCurrentChangedListener.ID_FILE_BROWSER_CURRENT_CHANGE_LIST);

        for(File f : files)
        {
            String name = f.getName();
            if(".".equals(name))
                continue;
            if(f.isDirectory())
                name += File.separator;

            item = new FileModel();
            item.name = name;
            item.path = f.getAbsolutePath();
            item.size = f.length();
            item.type = f.isDirectory() ? FileModel.ID_FILE_TYPE_DIRECTORY : FileModel.ID_FILE_TYPE_FILE;
            m_fileList.add(item);
        }

        Collections.sort(m_fileList, m_fileComparator);
        //m_fileList.sort(m_fileComparator);

        // 添加上级目录
        item = new FileModel();
        item.name = "../";
        item.path = dir.getParent();
        item.size = dir.length();
        item.type = dir.isDirectory() ? FileModel.ID_FILE_TYPE_DIRECTORY : FileModel.ID_FILE_TYPE_FILE;
        m_fileList.add(0, item);

        int mask = FileBrowserCurrentChangedListener.ID_FILE_BROWSER_CURRENT_CHANGE_LIST;
        if(m_currentPath != path)
        {
            m_currentPath = path;
            mask |= FileBrowserCurrentChangedListener.ID_FILE_BROWSER_CURRENT_CHANGE_PATH;
        }
        if(m_onCurrentChangedListener != null)
            m_onCurrentChangedListener.OnCurrentChanged(this, mask);

        return true;
    }

    public FileBrowser SetCurrentPath(String path)
    {
        if(m_currentPath != path)
        {
            if(ListFiles(path))
            {
                //m_currentPath = path;
                m_history.add(m_currentPath);
            }
        }
        return this;
    }

    public String CurrentPath() {
        return m_currentPath;
    }

    public List<FileModel> FileList() {
        return m_fileList;
    }

    public FileModel GetFileModel(int index)
    {
        if(index >= m_fileList.size())
            return null;
        return m_fileList.get(index);
    }

    public boolean ShowHidden() {
        return m_showHidden;
    }

    public FileBrowser SetShowHidden(boolean showHidden) {
        if(m_showHidden != showHidden)
        {
            this.m_showHidden = showHidden;
            ListFiles(m_currentPath);
        }
        return this;
    }

    public FileBrowser SetOnCurrentChangedListener(FileBrowserCurrentChangedListener listener) {
        if(m_onCurrentChangedListener != listener)
            m_onCurrentChangedListener = listener;
        return this;
    }

    public class FileModel
    {
        public static final int ID_FILE_TYPE_FILE = 0;
        public static final int ID_FILE_TYPE_DIRECTORY = 1;
        public static final int ID_FILE_TYPE_SYMBOL = 2;

        public String path;
        public String name;
        public long size;
        public int type;
        public String permission;
    }

    public interface FileBrowserCurrentChangedListener
    {
        public static final int ID_FILE_BROWSER_CURRENT_CHANGE_PATH = 1;
        public static final int ID_FILE_BROWSER_CURRENT_CHANGE_LIST = 1 << 1;
        public static final int ID_FILE_BROWSER_CURRENT_CHANGE_ALL = 0xff;
        public void OnCurrentChanged(FileBrowser browser, int mask);
    }

    private Comparator<FileModel> m_fileComparator = new Comparator<FileModel>(){
        @Override
        public int compare(FileModel a, FileModel b) {
            if("./".equals(a.name))
                return -1;
            if("../".equals(a.name))
                return -1;
            if(a.type != b.type)
            {
                if(a.type == FileModel.ID_FILE_TYPE_DIRECTORY)
                    return -1;
                if(b.type == FileModel.ID_FILE_TYPE_DIRECTORY)
                    return 1;
            }
            return a.name.compareToIgnoreCase(b.name);
        }
    };
}
