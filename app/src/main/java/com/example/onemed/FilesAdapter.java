package com.example.onemed;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private Context context;
    private List<Uri> fileUris;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "upload_prefs";
    private static final String KEY_FILES = "uploaded_files";

    public FilesAdapter(Context context, List<Uri> fileUris) {
        this.context = context;
        this.fileUris = fileUris;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_uploaded_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Uri uri = fileUris.get(position);
        String fileName = getFileName(uri);
        holder.fileName.setText(fileName);

        holder.clearBtn.setOnClickListener(v -> {
            fileUris.remove(position);
            saveUpdatedPrefs();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, fileUris.size());

            // Optionally hide the RecyclerView if empty
            if (fileUris.isEmpty() && context instanceof add_record_activity) {
                ((add_record_activity) context).hideRecyclerView();  // You need to implement this method
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileUris.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageButton clearBtn;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            clearBtn = itemView.findViewById(R.id.remove_upload);
        }
    }

    private void saveUpdatedPrefs() {
        Set<String> uris = new HashSet<>();
        for (Uri uri : fileUris) {
            uris.add(uri.toString());
        }
        prefs.edit().putStringSet(KEY_FILES, uris).apply();
    }

    private String getFileName(Uri uri) {
        String result = "Unnamed file";
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    result = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } else {
            result = new File(uri.getPath()).getName();
        }
        return result;
    }
}
