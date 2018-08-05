package com.xtensolution.nearbyconnection.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.xtensolution.connectionapi.model.MessageWrapper;
import com.xtensolution.nearbyconnection.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {

    private LayoutInflater inflater;
    private List<MessageWrapper> messages;

    public ChatAdapter(Context context, List<MessageWrapper> messages) {
        inflater = LayoutInflater.from(context);
        this.messages = messages;

    }

    public void addMessage(MessageWrapper message) {
        messages.add(messages.size(), message);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.chat_item_other, parent, false);
        if (viewType == 0)
            view = inflater.inflate(R.layout.chat_item_my, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isMyChat() ? 0 : 1;
    }

    static class ChatHolder extends RecyclerView.ViewHolder {
        private TextView txtMessage;
        private TextView txtUser;
        private TextView txtTimestamp;
        private SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, h:mm a");
        ;

        public ChatHolder(View itemView) {
            super(itemView);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtUser = itemView.findViewById(R.id.txtUsername);
        }

        public void bind(MessageWrapper message) {
            try {
                txtMessage.setText(message.getMessage());
                if (!message.isMyChat())
                    txtUser.setText(message.getEndpoint().getName());
                Date date = new Date();
                date.setTime(message.getTimestamp());
                txtTimestamp.setText(sdf.format(date));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
