/*
 * Copyright (C) 2021 - 2022 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.limboauth.command;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.elytrium.java.commons.mc.serialization.Serializer;
import net.elytrium.java.commons.mc.velocity.commands.SuggestUtils;
import net.elytrium.limboauth.LimboAuth;
import net.elytrium.limboauth.Settings;
import net.elytrium.limboauth.handler.AuthSessionHandler;
import net.elytrium.limboauth.model.RegisteredPlayer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.elytrium.limboauth.handler.AuthSessionHandler.genHash;

public class ForceRegisterCommand implements SimpleCommand {

  private final ProxyServer server;
  private final Dao<RegisteredPlayer, String> playerDao;

  private final String message;
  private final String successful;
  private final String notSuccessful;
  private final Component usage;

  public ForceRegisterCommand(ProxyServer server, Dao<RegisteredPlayer, String> playerDao) {
    this.server = server;
    this.playerDao = playerDao;

    this.message = Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_MESSAGE;
    this.successful = Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_SUCCESSFUL;
    this.notSuccessful = Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_NOT_SUCCESSFUL;
    this.usage = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_USAGE);
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    return SuggestUtils.suggestPlayers(this.server, invocation.arguments(), 0);
  }

  @Override
  public void execute(Invocation invocation) {
    CommandSource source = invocation.source();
    String[] args = invocation.arguments();

    if (args.length == 2) {
      String nickname = args[0];
      String newPassword = args[1];

      Serializer serializer = LimboAuth.getSerializer();
      try {
        RegisteredPlayer registeredPlayer = new RegisteredPlayer(
                nickname,
                nickname.toLowerCase(Locale.ROOT),
                genHash(newPassword),
                "",
                "",
                System.currentTimeMillis(),
                UUID.randomUUID().toString(),
                ""
        );

        this.playerDao.create(registeredPlayer);

        source.sendMessage(serializer.deserialize(MessageFormat.format(this.successful, nickname)), MessageType.SYSTEM);
      } catch (SQLException e) {
        source.sendMessage(serializer.deserialize(MessageFormat.format(this.notSuccessful, nickname)), MessageType.SYSTEM);
        e.printStackTrace();
      }
    } else {
      source.sendMessage(this.usage, MessageType.SYSTEM);
    }
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return invocation.source().hasPermission("limboauth.admin.forceregister");
  }
}
