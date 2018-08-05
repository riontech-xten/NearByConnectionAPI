package com.xtensolution.nearbyconnection.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xtensolution.connectionapi.service.ConnectionService;
import com.xtensolution.nearbyconnection.R;

import java.util.List;

public class EndpointAdapter extends RecyclerView.Adapter<EndpointAdapter.EndpointHolder> {

    private List<ConnectionService.Endpoint> endpoints;
    private LayoutInflater inflater;
    private View.OnClickListener onClickListener;

    public EndpointAdapter(Context context, List<ConnectionService.Endpoint> endpoints) {
        inflater = LayoutInflater.from(context);
        this.endpoints = endpoints;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void addEndpoint(ConnectionService.Endpoint endpoint) {
        endpoints.add(endpoint);
        notifyDataSetChanged();
    }

    public void remove(ConnectionService.Endpoint endpoint) {
        for (ConnectionService.Endpoint e : endpoints) {
            if (e.getId().equals(endpoint.getId())) {
                endpoints.remove(e);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void notifyUnreadCount(ConnectionService.Endpoint endpoint) {
        try {
            int index = 0;
            int count = 0;
            for (ConnectionService.Endpoint e : endpoints) {
                if (e.getId().equals(endpoint.getId())) {
                    count = e.getUnreadCount();
                    endpoints.remove(index);
                    break;
                }
                index = index + 1;
            }
            count = count + 1;
            endpoint.setUnreadCount(count);
            endpoints.add(0, endpoint);

            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ConnectionService.Endpoint> getEndpoints() {
        return endpoints;
    }

    @NonNull
    @Override
    public EndpointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.client_row_item, parent, false);
        return new EndpointHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EndpointHolder holder, int position) {
        holder.bind(endpoints.get(position));
    }

    @Override
    public int getItemCount() {
        return endpoints.size();
    }

    public class EndpointHolder extends RecyclerView.ViewHolder {

        private TextView txtEndpointName;
        private TextView txtEndpoint;
        private TextView txtUnreadCount;

        public EndpointHolder(View itemView) {
            super(itemView);
            txtEndpoint = itemView.findViewById(R.id.txtEndpoint);
            txtEndpointName = itemView.findViewById(R.id.txtName);
            txtUnreadCount = itemView.findViewById(R.id.txtUnreadCount);
        }

        public void bind(ConnectionService.Endpoint endpoint) {
            try {
                txtEndpointName.setText(endpoint.getName());
                txtEndpoint.setText(endpoint.getId());
                txtUnreadCount.setVisibility(View.GONE);
                if (endpoint.getUnreadCount() != 0) {
                    txtUnreadCount.setVisibility(View.VISIBLE);
                    txtUnreadCount.setText("" + endpoint.getUnreadCount());
                }
                itemView.setTag(endpoint);
                itemView.setOnClickListener(onClickListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
