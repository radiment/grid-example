package com.epam.learning.grid;

import com.epam.learning.Client;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.lang.IgniteCallable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.cache.Cache;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
        IgniteCache<Integer, Client> clients = ignite.cache("clients");
        return ignite.compute().broadcast((IgniteCallable<Integer>)
                () -> stream(clients.localEntries(CachePeekMode.PRIMARY).spliterator(), false)
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

}
