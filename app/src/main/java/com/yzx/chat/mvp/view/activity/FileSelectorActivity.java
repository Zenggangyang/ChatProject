package com.yzx.chat.mvp.view.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.DirectoryPathAdapter;
import com.yzx.chat.widget.adapter.FileAndDirectoryAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.SpacesItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class FileSelectorActivity extends BaseCompatActivity {
    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getPath();

    private RecyclerView mRvFileAndDirectory;
    private RecyclerView mRvDirectoryPath;
    private LinearLayoutManager mDirectoryPathLayoutManager;
    private FileAndDirectoryAdapter mFileAndDirectoryAdapter;
    private DirectoryPathAdapter mDirectoryPathAdapter;

    private String mCurrentPaht;
    private List<File> mCurrentFileList;
    private List<String> mDirectoryNameList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_file_selector;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRvFileAndDirectory = findViewById(R.id.FileSelectorActivity_mRvFileAndDirectory);
        mRvDirectoryPath = findViewById(R.id.FileSelectorActivity_mRvDirectoryPath);
        mCurrentFileList = new ArrayList<>();
        mDirectoryNameList = new ArrayList<>();
        mFileAndDirectoryAdapter = new FileAndDirectoryAdapter(mCurrentFileList);
        mDirectoryPathAdapter = new DirectoryPathAdapter(mDirectoryNameList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mDirectoryPathLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvDirectoryPath.setLayoutManager(mDirectoryPathLayoutManager);
        mRvDirectoryPath.setHasFixedSize(true);
        mRvDirectoryPath.addItemDecoration(new SpacesItemDecoration((int) AndroidUtil.dip2px(6), SpacesItemDecoration.HORIZONTAL));
        mRvDirectoryPath.addOnItemTouchListener(mOnPathItemClickListener);
        mRvDirectoryPath.setAdapter(mDirectoryPathAdapter);

        mRvFileAndDirectory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvFileAndDirectory.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.dividerColorBlack), DividerItemDecoration.ORIENTATION_HORIZONTAL));
        mRvFileAndDirectory.setHasFixedSize(true);
        mRvFileAndDirectory.addOnItemTouchListener(mOnFileOrDirectoryItemClickListener);
        mRvFileAndDirectory.setAdapter(mFileAndDirectoryAdapter);

        mDirectoryNameList.add(getString(R.string.FileSelectorActivity_Storage));
        mOnPathItemClickListener.onItemClick(0, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDirectoryNameList.size() == 1) {
            super.onBackPressed();
        } else {
            deleteDirectoryNameBehindOf(mDirectoryNameList.get(mDirectoryNameList.size() - 2));
            updateCurrentDirectoryContent();
        }
    }

    private void updateCurrentDirectoryContent() {
        StringBuilder stringBuilder = new StringBuilder(ROOT_PATH.length() + mDirectoryNameList.size() * 10);
        stringBuilder.append(ROOT_PATH);
        for (int i = 1, size = mDirectoryNameList.size(); i < size; i++) {
            stringBuilder.append("/").append(mDirectoryNameList.get(i));
        }
        mCurrentPaht = stringBuilder.toString();
        File[] files = new File(mCurrentPaht).listFiles();
        mCurrentFileList.clear();
        if (files != null) {
            File file;
            for (File file1 : files) {
                file = file1;
                if (!file.isHidden()) {
                    mCurrentFileList.add(file);
                }
            }
        }
        if (mCurrentFileList.size() > 0) {
            Collections.sort(mCurrentFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        mFileAndDirectoryAdapter.notifyDataSetChanged();
    }

    private void addDirectoryName(String directoryName) {
        mDirectoryPathAdapter.notifyItemInserted(mDirectoryNameList.size());
        mDirectoryNameList.add(directoryName);
        mDirectoryPathLayoutManager.scrollToPositionWithOffset(mDirectoryNameList.size() - 1, 0);
    }

    private void deleteDirectoryNameBehindOf(String directoryName) {
        int deleteCount;
        if (directoryName == null) {
            deleteCount = mDirectoryNameList.size() - 1;
            String first = mDirectoryNameList.get(0);
            mDirectoryNameList.clear();
            mDirectoryNameList.add(first);
        } else {
            deleteCount = 0;
            Iterator<String> it = mDirectoryNameList.iterator();
            boolean startDelete = false;
            while (it.hasNext()) {
                if (startDelete) {
                    it.next();
                    it.remove();
                    deleteCount++;
                    continue;
                }
                if (directoryName.equals(it.next())) {
                    startDelete = true;
                }
            }
        }
        mDirectoryPathAdapter.notifyItemRangeRemovedEx(mDirectoryNameList.size(), deleteCount);

    }

    private final OnRecyclerViewItemClickListener mOnPathItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
            if (position == 0) {
                deleteDirectoryNameBehindOf(null);
                updateCurrentDirectoryContent();
            } else if (position != mDirectoryNameList.size() - 1) {
                deleteDirectoryNameBehindOf(mDirectoryNameList.get(position));
                updateCurrentDirectoryContent();
            }
        }
    };

    private final OnRecyclerViewItemClickListener mOnFileOrDirectoryItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
            addDirectoryName(mCurrentFileList.get(position).getName());
            updateCurrentDirectoryContent();
        }
    };
}