package edu.aku.hassannaqvi.cs_ict.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.aku.hassannaqvi.cs_ict.R;
import edu.aku.hassannaqvi.cs_ict.contracts.FamilyMembersContract;
import edu.aku.hassannaqvi.cs_ict.databinding.ItemMemListBinding;
import edu.aku.hassannaqvi.cs_ict.viewmodel.MainVModel;

import static edu.aku.hassannaqvi.cs_ict.utils.UtilKt.getMemberIcon;

public class FamilyMemberListAdapter extends RecyclerView.Adapter<FamilyMemberListAdapter.ViewHolder> {

    private final Context mContext;
    private final MainVModel vModel;
    private OnItemClicked itemClicked;
    private List<FamilyMembersContract> mList;

    public FamilyMemberListAdapter(Context mContext, List<FamilyMembersContract> mList, MainVModel vModel) {
        this.mContext = mContext;
        this.mList = mList;
        this.vModel = vModel;
    }

    public void setItemClicked(OnItemClicked itemClicked) {
        this.itemClicked = itemClicked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        ItemMemListBinding bi = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_mem_list, viewGroup, false);
        return new ViewHolder(bi);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int i) {

        holder.bi.parentLayout.setTag(i);

        holder.bi.name.setText(mList.get(i).getName());
        String age = mList.get(i).getAge();
        holder.bi.dob.setText(new StringBuilder().append("Age:").append(Integer.parseInt(age) < 0 ? "-" : age).append(" Year(s)"));
        holder.bi.index.setText(String.format("%02d", Integer.valueOf(mList.get(i).getSerialno())));
        holder.bi.genderImage.setImageResource(getMemberIcon(Integer.parseInt(mList.get(i).getGender()), mList.get(i).getAge()));
        holder.bi.motherName.setText(new StringBuilder("Mother Name:").append(mList.get(i).getMotherName()));
        holder.bi.fatherName.setText(new StringBuilder("Father Name:").append(mList.get(i).getFatherName()));
        holder.bi.parentLayout.setOnClickListener(v -> {
            itemClicked.onItemClick(mList.get(i), i);
        });

        if (vModel.getCheckedItemValues(Integer.parseInt(mList.get(i).getSerialno()))) {
            holder.bi.checkIcon.setVisibility(View.VISIBLE);
            holder.bi.parentLayout.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setMList(List<FamilyMembersContract> members) {
        mList = members;
        notifyDataSetChanged();
    }

    public interface OnItemClicked {
        void onItemClick(FamilyMembersContract item, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemMemListBinding bi;

        ViewHolder(@NonNull ItemMemListBinding itemView) {
            super(itemView.getRoot());
            bi = itemView;
        }
    }
}
