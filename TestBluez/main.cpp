#include <string.h>
#include <stdlib.h>

#include <sys/types.h>
#include <unistd.h>

#include <time.h>

#include <gio/gio.h>


static void
on_name_appeared (GDBusConnection *connection,
                  const gchar     *name,
                  const gchar     *name_owner,
                  gpointer         user_data)
{
  gint fd;
  GError *error;

  error = NULL;
}

static void
on_name_vanished (GDBusConnection *connection,
                  const gchar     *name,
                  gpointer         user_data)
{
  g_printerr ("Failed to get name owner for %s\n"
              "Is ./gdbus-example-server running?\n",
              name);
  exit (1);
}

int
main (int argc, char *argv[])
{
  guint watcher_id;
  GMainLoop *loop;

  g_type_init ();

  watcher_id = g_bus_watch_name (G_BUS_TYPE_SESSION,
                                 "org.bluez",
                                 G_BUS_NAME_WATCHER_FLAGS_NONE,
                                 on_name_appeared,
                                 on_name_vanished,
                                 NULL,
                                 NULL);

  loop = g_main_loop_new (NULL, FALSE);
  g_main_loop_run (loop);

  g_bus_unwatch_name (watcher_id);
  return 0;
}
