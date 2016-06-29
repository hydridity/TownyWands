package de.fastfelix771.townywands.packets.v1_10;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import de.fastfelix771.townywands.main.TownyWands;
import de.fastfelix771.townywands.packets.PacketHandler;
import de.fastfelix771.townywands.utils.ReturningInvoker;
import lombok.SneakyThrows;

public class ProtocolLibHandler implements PacketHandler {

    @Override @SneakyThrows
    public void sendPacket(Player player, Object packet) {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, (PacketContainer) packet);
    }

    @Override
    public void addPacketListener(final Player player, final Object packetClass, final ReturningInvoker<Object, Boolean> invoker, final boolean dropPacketOnError) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TownyWands.getInstance(), PacketType.fromClass((Class<?>) packetClass)) {

            @Override
            public void onPacketReceiving(PacketEvent e) {
                if(!(e.getPlayer() == player)) return;
                if(!(e.getPacketType() == PacketType.fromClass((Class<?>) packetClass))) return;

                try{
                    e.setCancelled(invoker.invoke(e.getPacket()));
                } catch(Throwable t) {
                    t.printStackTrace();
                    e.setCancelled(dropPacketOnError);
                }

            }

        });
    }

}