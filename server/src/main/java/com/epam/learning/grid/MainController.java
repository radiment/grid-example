package com.epam.learning.grid;

import com.epam.learning.Client;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.apache.ignite.transactions.TransactionOptimisticException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.cache.Cache;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

@Path("/")
@Produces("application/json")
@Controller
public class MainController {

    @Autowired
    private Ignite ignite;

    @GET
    public Collection<Integer> main() {
        Collection<IgniteCallable<Integer>> calls = new ArrayList<>();

        // Iterate through all words in the sentence and create callable jobs.
        for (final String word : "Count characters using callable".split(" ")) {
            calls.add((IgniteCallable<Integer>) () -> {
                System.out.println();
                System.out.println(">>> Printing '" + word + "' on this node from ignite job.");

                return word.length();
            });
        }

        // Execute collection of callables on the ignite.
        return ignite.compute().call(calls);
    }

    @GET
    @Path("/clients")
    public List<Client> getClients() {
        List<Client> result = new ArrayList<>();
        IgniteCache<Integer, Client> clients = ignite.cache("clients");
        clients.forEach(entry -> result.add(entry.getValue()));
        Collections.sort(result, (o1, o2) -> o1.getId() - o2.getId());
        return result;
    }

    @GET
    @Path("/clients/sum")
    public int sum() {
        return ignite.compute().broadcast((IgniteCallable<Integer>)
                () -> stream(ignite.<Object, Client>cache("clients")
                        .localEntries(CachePeekMode.PRIMARY).spliterator(), false)
                        .mapToInt(value -> value.getValue().getBalance()).sum())
                .stream().mapToInt(Integer::intValue).sum();
    }

    @GET
    @Path("/clients/sums")
    public Collection<Integer> sums() {
        IgniteCache<Integer, Client> clients = ignite.cache("clients");
        return ignite.compute().broadcast((IgniteCallable<Integer>)
                () -> stream(clients.localEntries(CachePeekMode.PRIMARY).spliterator(), false)
                        .mapToInt(value -> value.getValue().getBalance()).sum());
    }

    @GET
    @Path("/clients/{type}")
    public Collection<Client> clients(final @PathParam("type") String type) {
        IgniteCache<Integer, Client> clients = ignite.cache("clients");
        return ignite.compute().affinityCall("clients", type,
                () -> stream(clients.localEntries(CachePeekMode.PRIMARY).spliterator(), false)
                        .map(Cache.Entry::getValue).collect(Collectors.toList()));
    }

    @GET
    @Path("/clients/node/{node}")
    public Collection<List<Client>> byNodes(final @PathParam("node") String node) {
        IgniteCache<Integer, Client> clients = ignite.cache("clients");

        return ignite.compute().broadcast((IgniteCallable<List<Client>>)
                () -> stream(clients.localEntries(CachePeekMode.PRIMARY).spliterator(), false)
                        .map(Cache.Entry::getValue).collect(Collectors.toList()));
    }

    @POST
    @Path("/clients")
    @Consumes("application/json")
    public boolean createClient(Client client) {
        return ignite.cache("clients").putIfAbsent(client.getId(), client);
    }

    @GET
    @Path("/transactions/long/{balance}")
    public boolean longTransaction(@PathParam("balance") Integer balance) throws InterruptedException {
        try (Transaction tx = ignite.transactions().txStart(
                TransactionConcurrency.PESSIMISTIC,
                TransactionIsolation.READ_COMMITTED)) {
            IgniteCache<Integer, Client> clients = ignite.cache("clients");
            clients.put(999, new Client(999, balance, "X"));

            System.out.println(">>>>>>>>>>>> pre-committed");

            Thread.sleep(50000);
            tx.rollback();
//                tx.commit();
            System.out.println(">>>>>>>>>>>> committed");
        } catch (TransactionOptimisticException e) {
            e.printStackTrace();
        }

        return true;

    }

    @GET
    @Path("/transactions")
    public Collection<Client> trans() {
        IgniteCache<Integer, Client> clients = ignite.cache("clients");
        List<Client> obtainedObjects = new ArrayList<>();

        try (Transaction tx = ignite.transactions().txStart(
                TransactionConcurrency.OPTIMISTIC,
                TransactionIsolation.READ_COMMITTED)) {
            Client client = clients.get(999);
            obtainedObjects.add(client);
//            tx.commit();
        } catch (TransactionOptimisticException e) {
            e.printStackTrace();
        }

/*
        try (Transaction tx = ignite.transactions().txStart(
                TransactionConcurrency.PESSIMISTIC,
                TransactionIsolation.SERIALIZABLE)) {
            Client client = clients.get(999);
            obtainedObjects.add(client);
//            tx.commit();
        } catch (TransactionOptimisticException e) {
            e.printStackTrace();
        }
*/

        Client client = clients.get(999);
        obtainedObjects.add(client);

        return obtainedObjects;
    }

}
