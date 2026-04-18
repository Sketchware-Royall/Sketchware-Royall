package pro.sketchware.activities.resourceseditor.components.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.activities.resourceseditor.ResourcesEditorActivity;
import pro.sketchware.activities.resourceseditor.components.adapters.IntsAdapter;
import pro.sketchware.activities.resourceseditor.components.models.IntModel;
import pro.sketchware.activities.resourceseditor.components.utils.IntsEditorManager;
import pro.sketchware.databinding.ResourcesEditorFragmentBinding;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.XmlUtil;

public class IntsEditor extends Fragment {
	
	private ResourcesEditorFragmentBinding binding;
	private ResourcesEditorActivity activity;
	
	public IntsAdapter adapter;
	public final ArrayList<IntModel> intsList = new ArrayList<>();
	private HashMap<Integer, String> notesMap = new HashMap<>();
	
	public final IntsEditorManager intsEditorManager = new IntsEditorManager();
	
	public boolean hasUnsavedChanges;
	private String filePath;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (ResourcesEditorActivity) getActivity();
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = ResourcesEditorFragmentBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	public void updateIntsList(String filePath, int updateMode, boolean hasUnsavedChangesStatus) {
		hasUnsavedChanges = hasUnsavedChangesStatus;
		this.filePath = filePath;
		
		boolean isSkippingMode = updateMode == 1;
		boolean isMergeAndReplace = updateMode == 2;
		
		ArrayList<IntModel> defaultInts = intsEditorManager.parseIntsFile(FileUtil.readFileIfExist(filePath));
		notesMap = new HashMap<>(intsEditorManager.notesMap);
		
		if (isSkippingMode) {
			HashSet<String> existingNames = new HashSet<>();
			for (IntModel existing : intsList) {
				existingNames.add(existing.getIntName());
			}
			
			for (IntModel model : defaultInts) {
				if (existingNames.add(model.getIntName())) {
					intsList.add(model);
				}
			}
		} else {
			if (isMergeAndReplace) {
				HashSet<String> newNames = new HashSet<>();
				for (IntModel model : defaultInts) {
					newNames.add(model.getIntName());
				}
				intsList.removeIf(existing -> newNames.contains(existing.getIntName()));
			} else {
				intsList.clear();
			}
			intsList.addAll(defaultInts);
		}
		
		activity.runOnUiThread(() -> {
			adapter = new IntsAdapter(intsList, activity, notesMap);
			binding.recyclerView.setAdapter(adapter);
			activity.checkForInvalidResources();
			updateNoContentLayout();
			if (hasUnsavedChanges) {
				this.filePath = activity.integersFilePath;
			}
		});
	}
	
	private void updateNoContentLayout() {
		if (intsList.isEmpty()) {
			binding.noContentLayout.setVisibility(View.VISIBLE);
			binding.noContentTitle.setText(String.format(Helper.getResString(R.string.resource_manager_no_list_title), "Integers"));
			binding.noContentBody.setText(String.format(Helper.getResString(R.string.resource_manager_no_list_body), "integer"));
		} else {
			binding.noContentLayout.setVisibility(View.GONE);
		}
	}
	
	public void showAddIntDialog() {
		showIntDialog(null, -1);
	}
	
	public void showEditIntDialog(int position) {
		if (position < 0 || position >= intsList.size()) return;
		showIntDialog(intsList.get(position), position);
	}
	
	private void showIntDialog(IntModel intModel, int position) {
		
		boolean isEditing = intModel != null;
		
		MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireActivity());
		
		LinearLayout root = new LinearLayout(requireContext());
		root.setOrientation(LinearLayout.VERTICAL);
		
		int pad = (int) (20 * getResources().getDisplayMetrics().density);
		int gap = (int) (12 * getResources().getDisplayMetrics().density);
		root.setPadding(pad, pad, pad, 0);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
		params.setMargins(0, 0, 0, gap);
		
		// Name
		TextInputLayout nameLayout = new TextInputLayout(
		requireContext(), null,
		com.google.android.material.R.attr.textInputStyle);
		nameLayout.setLayoutParams(params);
		nameLayout.setHint("Integer name");
		
		TextInputEditText nameInput = new TextInputEditText(nameLayout.getContext());
		
		nameInput.setSingleLine(true);
		
		nameInput.setFilters(new android.text.InputFilter[]{
			(s, a, b, d, c, e) -> s.toString().matches("[a-zA-Z0-9_]*") ? null : ""
		});
		
		nameLayout.addView(nameInput);
		
		// Value
		TextInputLayout valueLayout = new TextInputLayout(
		requireContext(), null,
		com.google.android.material.R.attr.textInputStyle);
		valueLayout.setLayoutParams(params);
		valueLayout.setHint("Integer value");
		
		TextInputEditText valueInput = new TextInputEditText(valueLayout.getContext());
		valueInput.setInputType(InputType.TYPE_CLASS_NUMBER);
		valueInput.setSingleLine(true);
		valueLayout.addView(valueInput);
		
		// Header
		TextInputLayout headerLayout = new TextInputLayout(
		requireContext(), null,
		com.google.android.material.R.attr.textInputStyle);
		headerLayout.setLayoutParams(params);
		headerLayout.setHint("Header note");
		
		TextInputEditText headerInput = new TextInputEditText(headerLayout.getContext());
		headerInput.setInputType(InputType.TYPE_CLASS_TEXT);
		headerInput.setSingleLine(false);
		headerLayout.addView(headerInput);
		
		root.addView(nameLayout);
		root.addView(valueLayout);
		root.addView(headerLayout);
		
		if (isEditing) {
			nameInput.setText(intModel.getIntName());
			valueInput.setText(intModel.getIntValue());
			headerInput.setText(notesMap.getOrDefault(position, ""));
		}
		
		dialog.setTitle(isEditing ? "Edit Integer" : "Create new Integer");
		dialog.setView(root);
		
		dialog.setPositiveButton(isEditing ? "Save" : "Create", (d, which) -> {
			
			String name = Objects.requireNonNull(nameInput.getText()).toString().trim();
			String value = Objects.requireNonNull(valueInput.getText()).toString().trim();
			String header = Objects.requireNonNull(headerInput.getText()).toString().trim();
			
			if (name.isEmpty() || value.isEmpty()) {
				SketchwareUtil.toastError("Please fill required fields");
				return;
			}
			
			if (!isInteger(value)) {
				SketchwareUtil.toastError("Invalid integer");
				return;
			}
			
			if (isEditing) {
				
				if (!name.equals(intModel.getIntName()) && isDuplicateName(name, position)) {
					SketchwareUtil.toastError("Already exists");
					return;
				}
				
				intModel.setIntName(name);
				intModel.setIntValue(value);
				
				if (header.isEmpty()) notesMap.remove(position);
				else notesMap.put(position, header);
				
				adapter.notifyItemChanged(position);
				
			} else {
				
				if (isDuplicateName(name, -1)) {
					SketchwareUtil.toastError("Already exists");
					return;
				}
				
				intsList.add(new IntModel(name, value));
				int pos = intsList.size() - 1;
				
				if (!header.isEmpty()) notesMap.put(pos, header);
				
				adapter.notifyItemInserted(pos);
			}
			
			updateNoContentLayout();
			hasUnsavedChanges = true;
		});
		
		dialog.setNegativeButton("Cancel", null);
		
		if (isEditing) {
			dialog.setNeutralButton("Delete", (d, w) -> {
				intsList.remove(position);
				notesMap.remove(position);
				adapter.notifyItemRemoved(position);
				updateNoContentLayout();
				hasUnsavedChanges = true;
			});
		}
		
		dialog.show();
	}
	
	private boolean isDuplicateName(String name, int ignore) {
		for (int i = 0; i < intsList.size(); i++) {
			if (i == ignore) continue;
			if (intsList.get(i).getIntName().equals(name)) return true;
		}
		return false;
	}
	
	private boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void saveIntsFile() {
		if (hasUnsavedChanges) {
			XmlUtil.saveXml(filePath,
			intsEditorManager.convertIntsToXML(intsList, notesMap));
			hasUnsavedChanges = false;
		}
	}
}
