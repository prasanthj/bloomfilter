BloomFilter [![Build Status](https://travis-ci.org/prasanthj/bloomfilter.svg?branch=master)](https://travis-ci.org/prasanthj/bloomfilter/branches)
===========

BloomFilters are probabilistic data structures for set membership check. BloomFilters are
highly space efficient when compared to using a HashSet. Because of the probabilistic nature of
bloom filter false positive (element not present in bloom filter but test() says true) are
possible but false negatives are not possible (if element is present then test() will never
say false). The false positive probability is configurable (default: 5%) depending on which
storage requirement may increase or decrease. Lower the false positive probability greater
is the space requirement.
Bloom filters are sensitive to number of elements that will be inserted in the bloom filter.
During the creation of bloom filter expected number of entries must be specified. If the number
of insertions exceed the specified initial number of entries then false positive probability will
increase accordingly. 
Internally, this implementation of bloom filter uses Murmur3 (from [here]) fast non-cryptographic hash
algorithm.

There are many excellent references for bloom filters. Some of them are
- http://billmill.org/bloomfilter-tutorial/
- http://www.jasondavies.com/bloomfilter/
- http://www.maxburstein.com/blog/creating-a-simple-bloom-filter/

Issues
------
Bug fixes or improvements are welcome! Please fork the project and send pull request on github. Or report issues here https://github.com/prasanthj/bloomfilter/issues


License
-------

Apache 2.0 licensed.

[here]:https://github.com/prasanthj/hasher
