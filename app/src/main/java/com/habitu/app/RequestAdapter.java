package com.habitu.app;

import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.List;
import java.util.Map;

public class RequestAdapter extends
        RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    List<Map<String, Object>> requests;
    String communityId;
    FirebaseFirestore db;
    Runnable onRefresh;

    public RequestAdapter(List<Map<String, Object>> requests,
                          String communityId,
                          FirebaseFirestore db,
                          Runnable onRefresh) {
        this.requests    = requests;
        this.communityId = communityId;
        this.db          = db;
        this.onRefresh   = onRefresh;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> req = requests.get(position);
        String userId     = (String) req.get("userId");
        String name       = (String) req.get("name");
        String university = (String) req.get("university");

        holder.tvName.setText(name);
        holder.tvUniversity.setText(university);
        holder.tvScore.setText("Habit tracker member");

        holder.btnAccept.setOnClickListener(v -> {
            WriteBatch batch = db.batch();
            DocumentReference ref = db.collection("communities").document(communityId);
            batch.update(ref, "memberIds",         FieldValue.arrayUnion(userId));
            batch.update(ref, "pendingRequestIds", FieldValue.arrayRemove(userId));
            batch.commit().addOnSuccessListener(x -> {
                Toast.makeText(v.getContext(),
                        name + " added to the community!", Toast.LENGTH_SHORT).show();
                onRefresh.run();
            });
        });

        holder.btnDecline.setOnClickListener(v ->
                db.collection("communities").document(communityId)
                        .update("pendingRequestIds", FieldValue.arrayRemove(userId))
                        .addOnSuccessListener(x -> onRefresh.run()));
    }

    @Override
    public int getItemCount() { return requests.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvUniversity, tvScore;
        Button btnAccept, btnDecline;
        ViewHolder(View v) {
            super(v);
            tvName       = v.findViewById(R.id.tvRequestName);
            tvUniversity = v.findViewById(R.id.tvRequestUniversity);
            tvScore      = v.findViewById(R.id.tvRequestScore);
            btnAccept    = v.findViewById(R.id.btnAccept);
            btnDecline   = v.findViewById(R.id.btnDecline);
        }
    }
}
