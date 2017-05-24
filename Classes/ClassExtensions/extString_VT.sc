+ String {
    expandNumberingPostfix{
        var result;
        case(
            {"^.+\\.\\d+$".matchRegexp(this)}, {
                result = this;
            },
            {"^.+\\.\\{.+\\}$".matchRegexp(this)}, {
                var name, numbers;
                #name, numbers = this.split($.);
                numbers = numbers.tr(${, $[).tr($}, $]).interpret;
                result = numbers.collect({arg item; "%.%".format(name, item)});
            },
            {"^.+\\[\\d+-\\d+]$".matchRegexp(this)}, {
                var name, numbers;
                #name, numbers = this.split($.);
                numbers = numbers.drop(1).drop(-1).split($-).collect(_.asInt);
                numbers = Array.series(numbers.maxItem - numbers.minItem + 1, numbers.minItem);
                result = numbers.collect({arg item;
                    "%.%".format(name, item);
                });
            },
            {
                result = this;
            }
        );
        ^result;
    }

    //this i a crude non-safe parse function, a hack that doesn't check for number of digits etc.
    parseMillisecondsFromTimeString{
        var result = 0, time, ms;
        #time, ms = this.split($.);//get the ms value first, if any;
        if(ms.notNil and: {"^\\d{3,3}$".matchRegexp(ms)}, {//if there is a ms value
            result = result + ms.asInteger;
        });
        time.split($:).reverseDo{arg item, i;
            var val;
            item = item.asInteger;
            val = switch(i,
                0, {item * 1000},
                1, {item * 60 * 1000},
                2, {item * 60 * 60 * 1000}
            );
            result = result + val;
        };
        ^result;
    }

	*makeRandomHexString { arg bytes = 8;
		^"0x%".format(
			String.newFrom(String.newFrom({"0123456789ABCDEF".choose} ! bytes))
		);
	}

	capitalize {
		^this[0].toUpper++this[1..]
	}

}