package pro.sketchware.activities.resourceseditor.components.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import pro.sketchware.activities.resourceseditor.ResourcesEditorActivity;
import pro.sketchware.activities.resourceseditor.components.fragments.IntsEditor;
import pro.sketchware.activities.resourceseditor.components.models.IntModel;
import pro.sketchware.databinding.PalletCustomviewBinding;

public class IntsAdapter extends RecyclerView.Adapter<IntsAdapter.ViewHolder> {

    private final ArrayList<IntModel> originalData;
    private final ResourcesEditorActivity activity;
    private final HashMap<Integer, String> notesMap;
    private ArrayList<IntModel> filteredData;

    public IntsAdapter(ArrayList<IntModel> data, ResourcesEditorActivity activity, HashMap<Integer, String> notesMap) {
        originalData = new ArrayList<>(data);
        filteredData = data;
        this.activity = activity;
        this.notesMap = notesMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PalletCustomviewBinding binding = PalletCustomviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IntModel intModel = filteredData.get(position);

        holder.binding.title.setText(intModel.getIntName());
        holder.binding.sub.setText(intModel.getIntValue());

        if (notesMap.containsKey(position)) {
            holder.binding.tvTitle.setText(notesMap.get(position));
            holder.binding.tvTitle.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvTitle.setVisibility(View.GONE);
        }

        holder.binding.backgroundCard.setOnClickListener(v -> {
            int adapterPosition = holder.getAbsoluteAdapterPosition();
            if (activity.intsEditor != null) {
                activity.intsEditor.showEditIntDialog(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void filter(String newText) {
        if (newText == null || newText.isEmpty()) {
            filteredData = new ArrayList<>(originalData);
        } else {
            String filterText = newText.toLowerCase().trim();
            filteredData = originalData.stream()
                    .filter(item -> item.getIntName().toLowerCase().contains(filterText)
                            || item.getIntValue().toLowerCase().contains(filterText))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public PalletCustomviewBinding binding;

        public ViewHolder(PalletCustomviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}